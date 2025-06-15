package com.bankingsystem.transactionservice.service;

import com.bankingsystem.transactionservice.dto.TransactionRequest;
import com.bankingsystem.transactionservice.dto.TransactionResponseDto;
import com.bankingsystem.transactionservice.exception.TransactionNotFoundException;
import com.bankingsystem.transactionservice.model.Transaction;
import com.bankingsystem.transactionservice.model.TransactionStatus;
import com.bankingsystem.transactionservice.model.TransactionType;
import com.bankingsystem.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private Transaction testTransaction;
    private List<Transaction> transactions;
    private Page<Transaction> transactionPage;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testTransaction = Transaction.builder()
                .id(1L)
                .transactionReference("TRX-12345")
                .sourceAccountNumber("1234567890123456")
                .destinationAccountNumber("6543210987654321")
                .amount(new BigDecimal("500.00"))
                .balanceAfterTransaction(new BigDecimal("1500.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description("Test transaction")
                .timestamp(now)
                .build();
                
        transactions = Arrays.asList(testTransaction);
        transactionPage = new PageImpl<>(transactions);
    }
    
    @Test
    void getAllTransactions_shouldReturnAllTransactions() {
        // Arrange
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(transactionPage);
        
        // Act
        Page<TransactionResponseDto> result = transactionService.getAllTransactions(PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("TRX-12345", result.getContent().get(0).getTransactionReference());
        verify(transactionRepository).findAll(any(Pageable.class));
    }
    
    @Test
    void getTransactionById_shouldReturnTransaction() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        
        // Act
        TransactionResponseDto result = transactionService.getTransactionById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TRX-12345", result.getTransactionReference());
        verify(transactionRepository).findById(1L);
    }
    
    @Test
    void getTransactionById_notFound_shouldThrowException() {
        // Arrange
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getTransactionById(99L);
        });
        verify(transactionRepository).findById(99L);
    }
    
    @Test
    void getTransactionByReference_shouldReturnTransaction() {
        // Arrange
        when(transactionRepository.findByTransactionReference("TRX-12345")).thenReturn(Optional.of(testTransaction));
        
        // Act
        TransactionResponseDto result = transactionService.getTransactionByReference("TRX-12345");
        
        // Assert
        assertNotNull(result);
        assertEquals("TRX-12345", result.getTransactionReference());
        verify(transactionRepository).findByTransactionReference("TRX-12345");
    }
    
    @Test
    void getTransactionByReference_notFound_shouldThrowException() {
        // Arrange
        when(transactionRepository.findByTransactionReference("INVALID")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getTransactionByReference("INVALID");
        });
        verify(transactionRepository).findByTransactionReference("INVALID");
    }
    
    @Test
    void getTransactionsBySourceAccount_shouldReturnTransactions() {
        // Arrange
        String accountNumber = "1234567890123456";
        when(transactionRepository.findBySourceAccountNumber(eq(accountNumber), any(Pageable.class)))
                .thenReturn(transactionPage);
        
        // Act
        Page<TransactionResponseDto> result = transactionService.getTransactionsBySourceAccount(
                accountNumber, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findBySourceAccountNumber(eq(accountNumber), any(Pageable.class));
    }
    
    @Test
    void getTransactionsByDestinationAccount_shouldReturnTransactions() {
        // Arrange
        String accountNumber = "6543210987654321";
        when(transactionRepository.findByDestinationAccountNumber(eq(accountNumber), any(Pageable.class)))
                .thenReturn(transactionPage);
        
        // Act
        Page<TransactionResponseDto> result = transactionService.getTransactionsByDestinationAccount(
                accountNumber, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findByDestinationAccountNumber(eq(accountNumber), any(Pageable.class));
    }
    
    @Test
    void getAccountTransactions_shouldReturnTransactions() {
        // Arrange
        String accountNumber = "1234567890123456";
        when(transactionRepository.findBySourceAccountNumberOrDestinationAccountNumber(
                eq(accountNumber), eq(accountNumber), any(Pageable.class))).thenReturn(transactionPage);
        
        // Act
        Page<TransactionResponseDto> result = transactionService.getAccountTransactions(
                accountNumber, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findBySourceAccountNumberOrDestinationAccountNumber(
                eq(accountNumber), eq(accountNumber), any(Pageable.class));
    }
    
    @Test
    void getTransactionsByDateRange_shouldReturnTransactions() {
        // Arrange
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);
        when(transactionRepository.findByTimestampBetween(eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(transactionPage);
        
        // Act
        Page<TransactionResponseDto> result = transactionService.getTransactionsByDateRange(
                startDate, endDate, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findByTimestampBetween(eq(startDate), eq(endDate), any(Pageable.class));
    }
    
    @Test
    void createTransaction_shouldCreateAndReturnTransaction() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountNumber("1234567890123456");
        request.setDestinationAccountNumber("6543210987654321");
        request.setAmount(new BigDecimal("500.00"));
        request.setType(TransactionType.TRANSFER);
        request.setDescription("Test transaction");
        
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });
        
        // Act
        TransactionResponseDto result = transactionService.createTransaction(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(request.getSourceAccountNumber(), result.getSourceAccountNumber());
        assertEquals(request.getDestinationAccountNumber(), result.getDestinationAccountNumber());
        assertEquals(request.getAmount(), result.getAmount());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        
        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("transaction-created"), anyString());
    }
    
    @Test
    void updateTransactionStatus_shouldUpdateAndReturnTransaction() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        
        // Act
        TransactionResponseDto result = transactionService.updateTransactionStatus(1L, TransactionStatus.FAILED);
        
        // Assert
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("status-update"), anyString());
    }
    
    @Test
    void updateTransactionStatus_notFound_shouldThrowException() {
        // Arrange
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.updateTransactionStatus(99L, TransactionStatus.FAILED);
        });
        verify(transactionRepository).findById(99L);
    }
}
