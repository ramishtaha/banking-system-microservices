package com.bankingsystem.transactionservice.repository;

import com.bankingsystem.transactionservice.model.Transaction;
import com.bankingsystem.transactionservice.model.TransactionStatus;
import com.bankingsystem.transactionservice.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private Transaction transaction1;
    private Transaction transaction2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        
        now = LocalDateTime.now();
        
        transaction1 = Transaction.builder()
                .transactionReference("TRX-12345")
                .sourceAccountNumber("1234567890123456")
                .destinationAccountNumber("6543210987654321")
                .amount(new BigDecimal("500.00"))
                .balanceAfterTransaction(new BigDecimal("1500.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description("Test transaction 1")
                .timestamp(now)
                .build();

        transaction2 = Transaction.builder()
                .transactionReference("TRX-67890")
                .sourceAccountNumber("1234567890123456")
                .destinationAccountNumber("9876543210987654")
                .amount(new BigDecimal("300.00"))
                .balanceAfterTransaction(new BigDecimal("1200.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transaction 2")
                .timestamp(now.minusDays(1))
                .build();
                
        transactionRepository.saveAll(List.of(transaction1, transaction2));
    }

    @Test
    void findByTransactionReference_shouldReturnTransaction() {
        // Act
        Optional<Transaction> result = transactionRepository.findByTransactionReference("TRX-12345");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("TRX-12345", result.get().getTransactionReference());
        assertEquals(TransactionStatus.COMPLETED, result.get().getStatus());
    }
    
    @Test
    void findBySourceAccountNumber_shouldReturnTransactions() {
        // Act
        Page<Transaction> result = transactionRepository.findBySourceAccountNumber(
                "1234567890123456", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(2, result.getTotalElements());
    }
    
    @Test
    void findByDestinationAccountNumber_shouldReturnTransactions() {
        // Act
        Page<Transaction> result = transactionRepository.findByDestinationAccountNumber(
                "6543210987654321", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("TRX-12345", result.getContent().get(0).getTransactionReference());
    }
    
    @Test
    void findBySourceAccountNumberOrDestinationAccountNumber_shouldReturnTransactions() {
        // Act
        Page<Transaction> result = transactionRepository.findBySourceAccountNumberOrDestinationAccountNumber(
                "9876543210987654", "9876543210987654", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("TRX-67890", result.getContent().get(0).getTransactionReference());
    }
    
    @Test
    void findByStatusAndTimestampBefore_shouldReturnTransactions() {
        // Act
        List<Transaction> result = transactionRepository.findByStatusAndTimestampBefore(
                TransactionStatus.PENDING, now);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("TRX-67890", result.get(0).getTransactionReference());
    }
    
    @Test
    void findByTimestampBetween_shouldReturnTransactions() {
        // Act
        Page<Transaction> result = transactionRepository.findByTimestampBetween(
                now.minusDays(2), now.plusDays(1), PageRequest.of(0, 10));
        
        // Assert
        assertEquals(2, result.getTotalElements());
    }
}
