package com.bankingsystem.accountservice.controller;

import com.bankingsystem.accountservice.dto.AccountCreationRequest;
import com.bankingsystem.accountservice.dto.AccountResponseDto;
import com.bankingsystem.accountservice.model.AccountType;
import com.bankingsystem.accountservice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private AccountResponseDto testAccountDto;
    private AccountCreationRequest creationRequest;
    private String accountNumber;

    @BeforeEach
    void setUp() {
        accountNumber = "1234567890123456";
        
        testAccountDto = AccountResponseDto.builder()
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
        List<AccountResponseDto> accounts = Arrays.asList(testAccountDto);
        when(accountService.getAllAccounts()).thenReturn(accounts);

        // Act
        ResponseEntity<List<AccountResponseDto>> response = accountController.getAllAccounts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testAccountDto.getId(), response.getBody().get(0).getId());
        verify(accountService).getAllAccounts();
    }

    @Test
    void getAccountById_shouldReturnAccount() {
        // Arrange
        when(accountService.getAccountById(1L)).thenReturn(testAccountDto);

        // Act
        ResponseEntity<AccountResponseDto> response = accountController.getAccountById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testAccountDto.getId(), response.getBody().getId());
        verify(accountService).getAccountById(1L);
    }

    @Test
    void getAccountByAccountNumber_shouldReturnAccount() {
        // Arrange
        when(accountService.getAccountByAccountNumber(accountNumber)).thenReturn(testAccountDto);

        // Act
        ResponseEntity<AccountResponseDto> response = accountController.getAccountByAccountNumber(accountNumber);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testAccountDto.getAccountNumber(), response.getBody().getAccountNumber());
        verify(accountService).getAccountByAccountNumber(accountNumber);
    }

    @Test
    void getAccountsByUserId_shouldReturnUserAccounts() {
        // Arrange
        List<AccountResponseDto> accounts = Arrays.asList(testAccountDto);
        when(accountService.getAccountsByUserId(1L)).thenReturn(accounts);

        // Act
        ResponseEntity<List<AccountResponseDto>> response = accountController.getAccountsByUserId(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testAccountDto.getUserId(), response.getBody().get(0).getUserId());
        verify(accountService).getAccountsByUserId(1L);
    }

    @Test
    void createAccount_shouldReturnCreatedAccount() {
        // Arrange
        when(accountService.createAccount(any(AccountCreationRequest.class))).thenReturn(testAccountDto);

        // Act
        ResponseEntity<AccountResponseDto> response = accountController.createAccount(creationRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testAccountDto.getId(), response.getBody().getId());
        verify(accountService).createAccount(creationRequest);
    }

    @Test
    void deposit_shouldReturnUpdatedAccount() {
        // Arrange
        BigDecimal amount = new BigDecimal("500.00");
        AccountResponseDto updatedAccount = AccountResponseDto.builder()
                .id(testAccountDto.getId())
                .accountNumber(testAccountDto.getAccountNumber())
                .balance(testAccountDto.getBalance().add(amount))
                .build();
                
        when(accountService.deposit(eq(accountNumber), eq(amount))).thenReturn(updatedAccount);

        // Act
        ResponseEntity<AccountResponseDto> response = accountController.deposit(accountNumber, amount);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAccount.getBalance(), response.getBody().getBalance());
        verify(accountService).deposit(accountNumber, amount);
    }

    @Test
    void withdraw_shouldReturnUpdatedAccount() {
        // Arrange
        BigDecimal amount = new BigDecimal("500.00");
        AccountResponseDto updatedAccount = AccountResponseDto.builder()
                .id(testAccountDto.getId())
                .accountNumber(testAccountDto.getAccountNumber())
                .balance(testAccountDto.getBalance().subtract(amount))
                .build();
                
        when(accountService.withdraw(eq(accountNumber), eq(amount))).thenReturn(updatedAccount);

        // Act
        ResponseEntity<AccountResponseDto> response = accountController.withdraw(accountNumber, amount);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAccount.getBalance(), response.getBody().getBalance());
        verify(accountService).withdraw(accountNumber, amount);
    }

    @Test
    void transfer_shouldReturnNoContent() {
        // Arrange
        String toAccountNumber = "6543210987654321";
        BigDecimal amount = new BigDecimal("300.00");
        doNothing().when(accountService).transfer(anyString(), anyString(), any(BigDecimal.class));

        // Act
        ResponseEntity<Void> response = accountController.transfer(accountNumber, toAccountNumber, amount);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService).transfer(accountNumber, toAccountNumber, amount);
    }

    @Test
    void deactivateAccount_shouldReturnDeactivatedAccount() {
        // Arrange
        AccountResponseDto deactivatedAccount = AccountResponseDto.builder()
                .id(testAccountDto.getId())
                .accountNumber(testAccountDto.getAccountNumber())
                .active(false)
                .build();
                
        when(accountService.deactivateAccount(1L)).thenReturn(deactivatedAccount);

        // Act
        ResponseEntity<AccountResponseDto> response = accountController.deactivateAccount(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isActive());
        verify(accountService).deactivateAccount(1L);
    }
}
