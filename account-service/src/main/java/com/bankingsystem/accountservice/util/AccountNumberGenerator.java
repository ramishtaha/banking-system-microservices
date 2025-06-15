package com.bankingsystem.accountservice.util;

import com.bankingsystem.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

    private static final String ACCOUNT_NUMBER_PREFIX = "1000";
    private static final int ACCOUNT_NUMBER_LENGTH = 16;

    @Autowired
    private AccountRepository accountRepository;

    private final SecureRandom random = new SecureRandom();

    public String generateAccountNumber() {
        String accountNumber;
        
        do {
            accountNumber = generateRandomAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }

    private String generateRandomAccountNumber() {
        StringBuilder sb = new StringBuilder(ACCOUNT_NUMBER_PREFIX);
        
        // Generate remaining digits to reach ACCOUNT_NUMBER_LENGTH
        int remainingLength = ACCOUNT_NUMBER_LENGTH - ACCOUNT_NUMBER_PREFIX.length();
        for (int i = 0; i < remainingLength; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
}
