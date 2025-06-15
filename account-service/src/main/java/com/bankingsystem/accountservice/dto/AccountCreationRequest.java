package com.bankingsystem.accountservice.dto;

import com.bankingsystem.accountservice.model.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    private BigDecimal initialDeposit;
    
    @Size(max = 100, message = "Account name cannot exceed 100 characters")
    private String accountName;
    
    private BigDecimal overdraftLimit;
    
    private BigDecimal interestRate;
}
