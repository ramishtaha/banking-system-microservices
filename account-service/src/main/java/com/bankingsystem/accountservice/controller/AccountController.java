package com.bankingsystem.accountservice.controller;

import com.bankingsystem.accountservice.dto.AccountCreationRequest;
import com.bankingsystem.accountservice.dto.AccountResponseDto;
import com.bankingsystem.accountservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        List<AccountResponseDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable Long id) {
        AccountResponseDto account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }
    
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByAccountNumber(@PathVariable String accountNumber) {
        AccountResponseDto account = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(account);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDto>> getAccountsByUserId(@PathVariable Long userId) {
        List<AccountResponseDto> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountCreationRequest request) {
        AccountResponseDto createdAccount = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponseDto> deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountResponseDto updatedAccount = accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponseDto> withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountResponseDto updatedAccount = accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber,
            @RequestParam BigDecimal amount) {
        accountService.transfer(fromAccountNumber, toAccountNumber, amount);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<AccountResponseDto> deactivateAccount(@PathVariable Long id) {
        AccountResponseDto updatedAccount = accountService.deactivateAccount(id);
        return ResponseEntity.ok(updatedAccount);
    }
}
