package com.bankingsystem.transactionservice.controller;

import com.bankingsystem.transactionservice.dto.TransactionRequest;
import com.bankingsystem.transactionservice.dto.TransactionResponseDto;
import com.bankingsystem.transactionservice.model.TransactionStatus;
import com.bankingsystem.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionResponseDto>> getAllTransactions(Pageable pageable) {
        Page<TransactionResponseDto> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long id) {
        TransactionResponseDto transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionResponseDto> getTransactionByReference(@PathVariable String reference) {
        TransactionResponseDto transaction = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/source/{accountNumber}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsBySourceAccount(
            @PathVariable String accountNumber, Pageable pageable) {
        Page<TransactionResponseDto> transactions = transactionService.getTransactionsBySourceAccount(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/destination/{accountNumber}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByDestinationAccount(
            @PathVariable String accountNumber, Pageable pageable) {
        Page<TransactionResponseDto> transactions = transactionService.getTransactionsByDestinationAccount(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<Page<TransactionResponseDto>> getAccountTransactions(
            @PathVariable String accountNumber, Pageable pageable) {
        Page<TransactionResponseDto> transactions = transactionService.getAccountTransactions(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/dateRange")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<TransactionResponseDto> transactions = transactionService.getTransactionsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponseDto transaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionResponseDto> updateTransactionStatus(
            @PathVariable Long id,
            @RequestParam TransactionStatus status) {
        TransactionResponseDto transaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(transaction);
    }
}
