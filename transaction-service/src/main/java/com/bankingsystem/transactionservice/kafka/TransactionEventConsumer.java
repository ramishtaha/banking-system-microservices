package com.bankingsystem.transactionservice.kafka;

import com.bankingsystem.transactionservice.model.Transaction;
import com.bankingsystem.transactionservice.model.TransactionStatus;
import com.bankingsystem.transactionservice.model.TransactionType;
import com.bankingsystem.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventConsumer.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @KafkaListener(topics = "transaction-events", groupId = "transaction-service")
    public void consumeTransactionEvents(String message) {
        logger.info("Received transaction event: {}", message);
        
        try {
            String[] parts = message.split(":", 2);
            if (parts.length != 2) {
                logger.error("Invalid transaction event format: {}", message);
                return;
            }
            
            String eventType = parts[0];
            String eventData = parts[1];
            
            switch (eventType) {
                case "deposit":
                    processDeposit(eventData);
                    break;
                case "withdrawal":
                    processWithdrawal(eventData);
                    break;
                case "transfer":
                    processTransfer(eventData);
                    break;
                default:
                    logger.warn("Unknown transaction event type: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Error processing transaction event: {}", e.getMessage(), e);
        }
    }
    
    private void processDeposit(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length != 3) {
                logger.error("Invalid deposit data format: {}", data);
                return;
            }
            
            String accountId = parts[0];
            BigDecimal amount = new BigDecimal(parts[1]);
            BigDecimal finalBalance = new BigDecimal(parts[2]);
            
            Transaction transaction = Transaction.builder()
                    .transactionReference(generateTransactionReference())
                    .type(TransactionType.DEPOSIT)
                    .sourceAccountNumber(accountId)
                    .destinationAccountNumber(accountId)
                    .amount(amount)
                    .balanceAfterTransaction(finalBalance)
                    .description("Deposit to account")
                    .timestamp(LocalDateTime.now())
                    .status(TransactionStatus.COMPLETED)
                    .build();
                    
            transactionRepository.save(transaction);
            logger.info("Deposit transaction recorded: {}", transaction.getTransactionReference());
        } catch (Exception e) {
            logger.error("Error processing deposit: {}", e.getMessage(), e);
        }
    }
    
    private void processWithdrawal(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length != 3) {
                logger.error("Invalid withdrawal data format: {}", data);
                return;
            }
            
            String accountId = parts[0];
            BigDecimal amount = new BigDecimal(parts[1]);
            BigDecimal finalBalance = new BigDecimal(parts[2]);
            
            Transaction transaction = Transaction.builder()
                    .transactionReference(generateTransactionReference())
                    .type(TransactionType.WITHDRAWAL)
                    .sourceAccountNumber(accountId)
                    .amount(amount)
                    .balanceAfterTransaction(finalBalance)
                    .description("Withdrawal from account")
                    .timestamp(LocalDateTime.now())
                    .status(TransactionStatus.COMPLETED)
                    .build();
                    
            transactionRepository.save(transaction);
            logger.info("Withdrawal transaction recorded: {}", transaction.getTransactionReference());
        } catch (Exception e) {
            logger.error("Error processing withdrawal: {}", e.getMessage(), e);
        }
    }
    
    private void processTransfer(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length != 4) {
                logger.error("Invalid transfer data format: {}", data);
                return;
            }
            
            String sourceAccountId = parts[0];
            String destinationAccountId = parts[1];
            BigDecimal amount = new BigDecimal(parts[2]);
            BigDecimal sourceBalance = new BigDecimal(parts[3]);
            
            Transaction transaction = Transaction.builder()
                    .transactionReference(generateTransactionReference())
                    .type(TransactionType.TRANSFER)
                    .sourceAccountNumber(sourceAccountId)
                    .destinationAccountNumber(destinationAccountId)
                    .amount(amount)
                    .balanceAfterTransaction(sourceBalance)
                    .description("Transfer between accounts")
                    .timestamp(LocalDateTime.now())
                    .status(TransactionStatus.COMPLETED)
                    .build();
                    
            transactionRepository.save(transaction);
            logger.info("Transfer transaction recorded: {}", transaction.getTransactionReference());
        } catch (Exception e) {
            logger.error("Error processing transfer: {}", e.getMessage(), e);
        }
    }
    
    private String generateTransactionReference() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
