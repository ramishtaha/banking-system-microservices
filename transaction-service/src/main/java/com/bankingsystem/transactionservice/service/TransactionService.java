package com.bankingsystem.transactionservice.service;

import com.bankingsystem.transactionservice.dto.TransactionRequest;
import com.bankingsystem.transactionservice.dto.TransactionResponseDto;
import com.bankingsystem.transactionservice.exception.TransactionNotFoundException;
import com.bankingsystem.transactionservice.model.Transaction;
import com.bankingsystem.transactionservice.model.TransactionStatus;
import com.bankingsystem.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public Page<TransactionResponseDto> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }
    
    public TransactionResponseDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + id));
        
        return mapToResponseDto(transaction);
    }
    
    public TransactionResponseDto getTransactionByReference(String transactionReference) {
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with reference: " + transactionReference));
        
        return mapToResponseDto(transaction);
    }
    
    public Page<TransactionResponseDto> getTransactionsBySourceAccount(String accountNumber, Pageable pageable) {
        return transactionRepository.findBySourceAccountNumber(accountNumber, pageable)
                .map(this::mapToResponseDto);
    }
    
    public Page<TransactionResponseDto> getTransactionsByDestinationAccount(String accountNumber, Pageable pageable) {
        return transactionRepository.findByDestinationAccountNumber(accountNumber, pageable)
                .map(this::mapToResponseDto);
    }
    
    public Page<TransactionResponseDto> getAccountTransactions(String accountNumber, Pageable pageable) {
        return transactionRepository.findBySourceAccountNumberOrDestinationAccountNumber(accountNumber, accountNumber, pageable)
                .map(this::mapToResponseDto);
    }
    
    public Page<TransactionResponseDto> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByTimestampBetween(startDate, endDate, pageable)
                .map(this::mapToResponseDto);
    }
    
    public TransactionResponseDto createTransaction(TransactionRequest request) {
        String transactionReference = generateTransactionReference();
        
        Transaction transaction = Transaction.builder()
                .transactionReference(transactionReference)
                .type(request.getType())
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Send transaction event to Kafka
        // The specific handling would depend on the transaction type
        // For this example, we'll just publish a simple message
        kafkaTemplate.send("transaction-events", "transaction-created", savedTransaction.getId().toString());
        
        return mapToResponseDto(savedTransaction);
    }
    
    public TransactionResponseDto updateTransactionStatus(Long id, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + id));
        
        transaction.setStatus(status);
        Transaction updatedTransaction = transactionRepository.save(transaction);
        
        // Send status update event to Kafka
        kafkaTemplate.send("transaction-events", "status-update", 
                String.format("%s:%s", updatedTransaction.getId(), status));
        
        return mapToResponseDto(updatedTransaction);
    }
    
    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .type(transaction.getType())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .destinationAccountNumber(transaction.getDestinationAccountNumber())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .build();
    }
    
    private String generateTransactionReference() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
