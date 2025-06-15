package com.bankingsystem.accountservice.service;

import com.bankingsystem.accountservice.client.UserServiceClient;
import com.bankingsystem.accountservice.dto.AccountCreationRequest;
import com.bankingsystem.accountservice.dto.AccountResponseDto;
import com.bankingsystem.accountservice.dto.UserDto;
import com.bankingsystem.accountservice.exception.AccountNotFoundException;
import com.bankingsystem.accountservice.exception.InsufficientFundsException;
import com.bankingsystem.accountservice.model.Account;
import com.bankingsystem.accountservice.model.AccountType;
import com.bankingsystem.accountservice.repository.AccountRepository;
import com.bankingsystem.accountservice.util.AccountNumberGenerator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private AccountNumberGenerator accountNumberGenerator;
    
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public AccountResponseDto getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
        
        return mapToResponseDto(account);
    }
    
    public List<AccountResponseDto> getAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public AccountResponseDto getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));
        
        return mapToResponseDto(account);
    }
    
    @Transactional
    public AccountResponseDto createAccount(AccountCreationRequest request) {
        // Validate user exists (would be done via userServiceClient)
        
        String accountNumber = accountNumberGenerator.generateAccountNumber();
        
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO)
                .userId(request.getUserId())
                .accountName(request.getAccountName())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Set account type specific fields
        switch (request.getAccountType()) {
            case CHECKING:
                account.setOverdraftLimit(request.getOverdraftLimit() != null ? 
                        request.getOverdraftLimit() : BigDecimal.ZERO);
                break;
            case SAVINGS:
                account.setInterestRate(request.getInterestRate() != null ? 
                        request.getInterestRate() : new BigDecimal("0.01")); // Default 1%
                break;
            case CREDIT:
                // Credit account specific setup
                break;
        }
        
        Account savedAccount = accountRepository.save(account);
        
        // Send account created event to Kafka
        kafkaTemplate.send("account-events", "account-created", savedAccount.getId().toString());
        
        return mapToResponseDto(savedAccount);
    }
    
    @Transactional
    public AccountResponseDto deactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
        
        account.setActive(false);
        account.setUpdatedAt(LocalDateTime.now());
        
        Account updatedAccount = accountRepository.save(account);
        
        // Send account deactivated event to Kafka
        kafkaTemplate.send("account-events", "account-deactivated", updatedAccount.getId().toString());
        
        return mapToResponseDto(updatedAccount);
    }
    
    @Transactional
    public AccountResponseDto deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));
        
        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());
        
        Account updatedAccount = accountRepository.save(account);
        
        // Send transaction event to Kafka
        kafkaTemplate.send("transaction-events", "deposit", 
                String.format("%s:%s:%s", account.getId(), amount, updatedAccount.getBalance()));
        
        return mapToResponseDto(updatedAccount);
    }
    
    @Transactional
    public AccountResponseDto withdraw(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));
        
        BigDecimal availableFunds = account.getBalance();
        if (account.getAccountType() == AccountType.CHECKING && account.getOverdraftLimit() != null) {
            availableFunds = availableFunds.add(account.getOverdraftLimit());
        }
        
        if (availableFunds.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(LocalDateTime.now());
        
        Account updatedAccount = accountRepository.save(account);
        
        // Send transaction event to Kafka
        kafkaTemplate.send("transaction-events", "withdrawal", 
                String.format("%s:%s:%s", account.getId(), amount, updatedAccount.getBalance()));
        
        return mapToResponseDto(updatedAccount);
    }
    
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + fromAccountNumber));
        
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + toAccountNumber));
        
        BigDecimal availableFunds = fromAccount.getBalance();
        if (fromAccount.getAccountType() == AccountType.CHECKING && fromAccount.getOverdraftLimit() != null) {
            availableFunds = availableFunds.add(fromAccount.getOverdraftLimit());
        }
        
        if (availableFunds.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }
        
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        fromAccount.setUpdatedAt(LocalDateTime.now());
        
        toAccount.setBalance(toAccount.getBalance().add(amount));
        toAccount.setUpdatedAt(LocalDateTime.now());
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Send transfer event to Kafka
        kafkaTemplate.send("transaction-events", "transfer", 
                String.format("%s:%s:%s:%s", fromAccount.getId(), toAccount.getId(), amount, fromAccount.getBalance()));
    }
    
    private AccountResponseDto mapToResponseDto(Account account) {
        AccountResponseDto dto = AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .overdraftLimit(account.getOverdraftLimit())
                .interestRate(account.getInterestRate())
                .userId(account.getUserId())
                .accountName(account.getAccountName())
                .active(account.isActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
        
        // Optionally fetch user details
        try {
            UserDto userDto = userServiceClient.getUserById(account.getUserId());
            dto.setUser(userDto);
        } catch (Exception e) {
            // User service may be down, continue without user details
        }
        
        return dto;
    }
}
