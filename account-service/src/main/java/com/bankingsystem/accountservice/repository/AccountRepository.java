package com.bankingsystem.accountservice.repository;

import com.bankingsystem.accountservice.model.Account;
import com.bankingsystem.accountservice.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    List<Account> findByUserId(Long userId);
    
    List<Account> findByUserIdAndAccountType(Long userId, AccountType accountType);
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    boolean existsByAccountNumber(String accountNumber);
}
