package com.bankingsystem.transactionservice.repository;

import com.bankingsystem.transactionservice.model.Transaction;
import com.bankingsystem.transactionservice.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    Page<Transaction> findBySourceAccountNumber(String sourceAccountNumber, Pageable pageable);
    
    Page<Transaction> findByDestinationAccountNumber(String destinationAccountNumber, Pageable pageable);
    
    Page<Transaction> findBySourceAccountNumberOrDestinationAccountNumber(
            String sourceAccountNumber, String destinationAccountNumber, Pageable pageable);
    
    List<Transaction> findByStatusAndTimestampBefore(TransactionStatus status, LocalDateTime timestamp);
    
    Page<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
