package com.bankingsystem.transactionservice.controller;

import com.bankingsystem.transactionservice.dto.TransactionRequest;
import com.bankingsystem.transactionservice.dto.TransactionResponseDto;
import com.bankingsystem.transactionservice.model.TransactionStatus;
import com.bankingsystem.transactionservice.model.TransactionType;
import com.bankingsystem.transactionservice.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionResponseDto testTransaction;
    private TransactionRequest transactionRequest;
    private String referenceNumber;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        referenceNumber = "TRX-12345";        testTransaction = TransactionResponseDto.builder()
                .id(1L)
                .transactionReference(referenceNumber)
                .sourceAccountNumber("1234567890123456")
                .destinationAccountNumber("6543210987654321")
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .timestamp(now)
                .description("Test transaction")
                .build();

        transactionRequest = new TransactionRequest();
        transactionRequest.setSourceAccountNumber("1234567890123456");
        transactionRequest.setDestinationAccountNumber("6543210987654321");
        transactionRequest.setAmount(new BigDecimal("500.00"));
        transactionRequest.setType(TransactionType.TRANSFER);
        transactionRequest.setDescription("Test transaction");
    }

    @Test
    void getAllTransactions_shouldReturnAllTransactions() {
        // Arrange
        List<TransactionResponseDto> transactions = Arrays.asList(testTransaction);
        Page<TransactionResponseDto> page = new PageImpl<>(transactions);
        
        when(transactionService.getAllTransactions(any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<TransactionResponseDto>> response = transactionController.getAllTransactions(Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(transactionService).getAllTransactions(any(Pageable.class));
    }

    @Test
    void getTransactionById_shouldReturnTransaction() {
        // Arrange
        when(transactionService.getTransactionById(1L)).thenReturn(testTransaction);

        // Act
        ResponseEntity<TransactionResponseDto> response = transactionController.getTransactionById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTransaction.getId(), response.getBody().getId());
        verify(transactionService).getTransactionById(1L);
    }

    @Test
    void getTransactionByReference_shouldReturnTransaction() {
        // Arrange
        when(transactionService.getTransactionByReference(referenceNumber)).thenReturn(testTransaction);

        // Act
        ResponseEntity<TransactionResponseDto> response = transactionController.getTransactionByReference(referenceNumber);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(referenceNumber, response.getBody().getTransactionReference());
        verify(transactionService).getTransactionByReference(referenceNumber);
    }

    @Test
    void getTransactionsBySourceAccount_shouldReturnTransactions() {
        // Arrange
        String sourceAccount = "1234567890123456";
        List<TransactionResponseDto> transactions = Arrays.asList(testTransaction);
        Page<TransactionResponseDto> page = new PageImpl<>(transactions);
        
        when(transactionService.getTransactionsBySourceAccount(eq(sourceAccount), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<TransactionResponseDto>> response = 
            transactionController.getTransactionsBySourceAccount(sourceAccount, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(transactionService).getTransactionsBySourceAccount(eq(sourceAccount), any(Pageable.class));
    }

    @Test
    void getTransactionsByDestinationAccount_shouldReturnTransactions() {
        // Arrange
        String destAccount = "6543210987654321";
        List<TransactionResponseDto> transactions = Arrays.asList(testTransaction);
        Page<TransactionResponseDto> page = new PageImpl<>(transactions);
        
        when(transactionService.getTransactionsByDestinationAccount(eq(destAccount), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<TransactionResponseDto>> response = 
            transactionController.getTransactionsByDestinationAccount(destAccount, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(transactionService).getTransactionsByDestinationAccount(eq(destAccount), any(Pageable.class));
    }

    @Test
    void getAccountTransactions_shouldReturnTransactions() {
        // Arrange
        String accountNumber = "1234567890123456";
        List<TransactionResponseDto> transactions = Arrays.asList(testTransaction);
        Page<TransactionResponseDto> page = new PageImpl<>(transactions);
        
        when(transactionService.getAccountTransactions(eq(accountNumber), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<TransactionResponseDto>> response = 
            transactionController.getAccountTransactions(accountNumber, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(transactionService).getAccountTransactions(eq(accountNumber), any(Pageable.class));
    }

    @Test
    void getTransactionsByDateRange_shouldReturnTransactions() {
        // Arrange
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);
        List<TransactionResponseDto> transactions = Arrays.asList(testTransaction);
        Page<TransactionResponseDto> page = new PageImpl<>(transactions);
        
        when(transactionService.getTransactionsByDateRange(
                eq(startDate), eq(endDate), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<TransactionResponseDto>> response = 
            transactionController.getTransactionsByDateRange(startDate, endDate, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(transactionService).getTransactionsByDateRange(eq(startDate), eq(endDate), any(Pageable.class));
    }

    @Test
    void createTransaction_shouldReturnCreatedTransaction() {
        // Arrange
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(testTransaction);

        // Act
        ResponseEntity<TransactionResponseDto> response = transactionController.createTransaction(transactionRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testTransaction.getId(), response.getBody().getId());
        verify(transactionService).createTransaction(transactionRequest);
    }    @Test
    void updateTransactionStatus_shouldReturnUpdatedTransaction() {
        // Arrange
        TransactionResponseDto updatedTransaction = TransactionResponseDto.builder()
                .id(1L)
                .transactionReference(referenceNumber)
                .status(TransactionStatus.FAILED)
                .build();
                
        when(transactionService.updateTransactionStatus(eq(1L), eq(TransactionStatus.FAILED))).thenReturn(updatedTransaction);

        // Act
        ResponseEntity<TransactionResponseDto> response = 
            transactionController.updateTransactionStatus(1L, TransactionStatus.FAILED);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TransactionStatus.FAILED, response.getBody().getStatus());
        verify(transactionService).updateTransactionStatus(1L, TransactionStatus.FAILED);
    }
}
