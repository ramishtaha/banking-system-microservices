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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private Account secondAccount;
    private AccountCreationRequest creationRequest;
    private String accountNumber;
    private String secondAccountNumber;

    @BeforeEach
    void setUp() {
        accountNumber = "1234567890123456";
        secondAccountNumber = "6543210987654321";
        
        testAccount = Account.builder()
                .id(1L)
                .accountNumber(accountNumber)
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .overdraftLimit(new BigDecimal("500.00"))
                .userId(1L)
                .accountName("Test Checking Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
                
        secondAccount = Account.builder()
                .id(2L)
                .accountNumber(secondAccountNumber)
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .interestRate(new BigDecimal("0.02"))
                .userId(1L)
                .accountName("Test Savings Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        creationRequest = new AccountCreationRequest();
        creationRequest.setUserId(1L);
        creationRequest.setAccountType(AccountType.CHECKING);
        creationRequest.setAccountName("Test Checking Account");
        creationRequest.setInitialDeposit(new BigDecimal("1000.00"));
        creationRequest.setOverdraftLimit(new BigDecimal("500.00"));
    }

    @Test
    void getAllAccounts_shouldReturnAllAccounts() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount, secondAccount);
        when(accountRepository.findAll()).thenReturn(accounts);

        // Act
        List<AccountResponseDto> result = accountService.getAllAccounts();

        // Assert
        assertEquals(2, result.size());
        assertEquals(testAccount.getId(), result.get(0).getId());
        assertEquals(secondAccount.getId(), result.get(1).getId());
        verify(accountRepository).findAll();
    }

    @Test
    void getAccountById_whenAccountExists_shouldReturnAccount() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        AccountResponseDto result = accountService.getAccountById(1L);

        // Assert
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getAccountNumber(), result.getAccountNumber());
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountById_whenAccountDoesNotExist_shouldThrowNotFoundException() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(999L));
        verify(accountRepository).findById(999L);
    }

    @Test
    void getAccountsByUserId_shouldReturnUserAccounts() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount, secondAccount);
        when(accountRepository.findByUserId(1L)).thenReturn(accounts);

        // Act
        List<AccountResponseDto> result = accountService.getAccountsByUserId(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals(testAccount.getId(), result.get(0).getId());
        assertEquals(secondAccount.getId(), result.get(1).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(1L, result.get(1).getUserId());
        verify(accountRepository).findByUserId(1L);
    }

    @Test
    void getAccountByAccountNumber_whenAccountExists_shouldReturnAccount() {
        // Arrange
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));

        // Act
        AccountResponseDto result = accountService.getAccountByAccountNumber(accountNumber);

        // Assert
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(accountNumber, result.getAccountNumber());
        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void getAccountByAccountNumber_whenAccountDoesNotExist_shouldThrowNotFoundException() {
        // Arrange
        String invalidAccountNumber = "9999999999999999";
        when(accountRepository.findByAccountNumber(invalidAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByAccountNumber(invalidAccountNumber));
        verify(accountRepository).findByAccountNumber(invalidAccountNumber);
    }

    @Test
    void createAccount_shouldCreateAndReturnAccount() {
        // Arrange
        when(accountNumberGenerator.generateAccountNumber()).thenReturn(accountNumber);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        AccountResponseDto result = accountService.createAccount(creationRequest);

        // Assert
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(accountNumber, result.getAccountNumber());
        assertEquals(creationRequest.getAccountType(), result.getAccountType());
        assertEquals(creationRequest.getInitialDeposit(), result.getBalance());
        verify(accountNumberGenerator).generateAccountNumber();
        verify(accountRepository).save(any(Account.class));
        verify(kafkaTemplate).send(eq("account-events"), eq("account-created"), anyString());
    }

    @Test
    void deactivateAccount_shouldDeactivateAndReturnAccount() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        
        Account deactivatedAccount = new Account();
        deactivatedAccount.setId(testAccount.getId());
        deactivatedAccount.setAccountNumber(testAccount.getAccountNumber());
        deactivatedAccount.setActive(false);
        
        when(accountRepository.save(any(Account.class))).thenReturn(deactivatedAccount);

        // Act
        AccountResponseDto result = accountService.deactivateAccount(1L);

        // Assert
        assertFalse(result.isActive());
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
        verify(kafkaTemplate).send(eq("account-events"), eq("account-deactivated"), anyString());
    }

    @Test
    void deposit_withValidAmount_shouldUpdateBalanceAndReturnAccount() {
        // Arrange
        BigDecimal depositAmount = new BigDecimal("500.00");
        BigDecimal newBalance = testAccount.getBalance().add(depositAmount);
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));
        
        Account updatedAccount = new Account();
        updatedAccount.setId(testAccount.getId());
        updatedAccount.setAccountNumber(testAccount.getAccountNumber());
        updatedAccount.setBalance(newBalance);
        
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // Act
        AccountResponseDto result = accountService.deposit(accountNumber, depositAmount);

        // Assert
        assertEquals(newBalance, result.getBalance());
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(any(Account.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("deposit"), anyString());
    }

    @Test
    void deposit_withNegativeAmount_shouldThrowException() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> accountService.deposit(accountNumber, negativeAmount));
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdraw_withSufficientFunds_shouldUpdateBalanceAndReturnAccount() {
        // Arrange
        BigDecimal withdrawAmount = new BigDecimal("300.00");
        BigDecimal newBalance = testAccount.getBalance().subtract(withdrawAmount);
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));
        
        Account updatedAccount = new Account();
        updatedAccount.setId(testAccount.getId());
        updatedAccount.setAccountNumber(testAccount.getAccountNumber());
        updatedAccount.setBalance(newBalance);
        
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // Act
        AccountResponseDto result = accountService.withdraw(accountNumber, withdrawAmount);

        // Assert
        assertEquals(newBalance, result.getBalance());
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(any(Account.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("withdrawal"), anyString());
    }

    @Test
    void withdraw_withInsufficientFunds_shouldThrowException() {
        // Arrange
        BigDecimal excessiveAmount = new BigDecimal("2000.00");
        // Account has 1000 balance, 500 overdraft limit, so 1500 total available
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> accountService.withdraw(accountNumber, excessiveAmount));
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transfer_withSufficientFunds_shouldUpdateBothAccounts() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("300.00");
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber(secondAccountNumber)).thenReturn(Optional.of(secondAccount));
        
        // Act
        accountService.transfer(accountNumber, secondAccountNumber, transferAmount);

        // Assert
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).findByAccountNumber(secondAccountNumber);
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("transfer"), anyString());
    }

    @Test
    void transfer_toSameAccount_shouldThrowException() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> accountService.transfer(accountNumber, accountNumber, amount));
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transfer_withInsufficientFunds_shouldThrowException() {
        // Arrange
        BigDecimal excessiveAmount = new BigDecimal("2000.00");
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber(secondAccountNumber)).thenReturn(Optional.of(secondAccount));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, 
            () -> accountService.transfer(accountNumber, secondAccountNumber, excessiveAmount));
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).findByAccountNumber(secondAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }
}
