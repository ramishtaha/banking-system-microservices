package com.bankingsystem.transactionservice.dto;

import com.bankingsystem.transactionservice.model.TransactionStatus;
import com.bankingsystem.transactionservice.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    private Long id;
    private String transactionReference;
    private TransactionType type;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private String description;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
