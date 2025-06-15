package com.bankingsystem.accountservice.repository;

import com.bankingsystem.accountservice.model.Account;
import com.bankingsystem.accountservice.model.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=true",
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false",
    "spring.cloud.config.import-check.enabled=false",
    "spring.config.import=optional:configserver:",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByUserId_shouldReturnUserAccounts() {
        // Arrange
        Account checkingAccount = Account.builder()
                .accountNumber("1234567890123456")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .overdraftLimit(new BigDecimal("500.00"))
                .userId(1L)
                .accountName("Test Checking Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
                
        Account savingsAccount = Account.builder()
                .accountNumber("6543210987654321")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .interestRate(new BigDecimal("0.02"))
                .userId(1L)
                .accountName("Test Savings Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
                
        Account otherUserAccount = Account.builder()
                .accountNumber("9876543210123456")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("3000.00"))
                .userId(2L)
                .accountName("Other User Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        entityManager.persist(checkingAccount);
        entityManager.persist(savingsAccount);
        entityManager.persist(otherUserAccount);
        entityManager.flush();

        // Act
        List<Account> userAccounts = accountRepository.findByUserId(1L);

        // Assert
        assertEquals(2, userAccounts.size());
        assertTrue(userAccounts.stream()
                .anyMatch(account -> account.getAccountNumber().equals("1234567890123456")));
        assertTrue(userAccounts.stream()
                .anyMatch(account -> account.getAccountNumber().equals("6543210987654321")));
        assertFalse(userAccounts.stream()
                .anyMatch(account -> account.getAccountNumber().equals("9876543210123456")));
    }

    @Test
    void findByUserIdAndAccountType_shouldReturnFilteredAccounts() {
        // Arrange
        Account checkingAccount = Account.builder()
                .accountNumber("1234567890123456")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .userId(1L)
                .accountName("Test Checking Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
                
        Account savingsAccount = Account.builder()
                .accountNumber("6543210987654321")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .userId(1L)
                .accountName("Test Savings Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        entityManager.persist(checkingAccount);
        entityManager.persist(savingsAccount);
        entityManager.flush();

        // Act
        List<Account> checkingAccounts = accountRepository.findByUserIdAndAccountType(1L, AccountType.CHECKING);
        List<Account> savingsAccounts = accountRepository.findByUserIdAndAccountType(1L, AccountType.SAVINGS);

        // Assert
        assertEquals(1, checkingAccounts.size());
        assertEquals(1, savingsAccounts.size());
        assertEquals("1234567890123456", checkingAccounts.get(0).getAccountNumber());
        assertEquals("6543210987654321", savingsAccounts.get(0).getAccountNumber());
    }

    @Test
    void findByAccountNumber_shouldReturnAccount() {
        // Arrange
        String accountNumber = "1234567890123456";
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .userId(1L)
                .accountName("Test Checking Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        entityManager.persist(account);
        entityManager.flush();

        // Act
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(accountNumber);

        // Assert
        assertTrue(foundAccount.isPresent());
        assertEquals(accountNumber, foundAccount.get().getAccountNumber());
    }

    @Test
    void existsByAccountNumber_whenAccountExists_shouldReturnTrue() {
        // Arrange
        String accountNumber = "1234567890123456";
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .userId(1L)
                .accountName("Test Checking Account")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        entityManager.persist(account);
        entityManager.flush();

        // Act
        boolean exists = accountRepository.existsByAccountNumber(accountNumber);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByAccountNumber_whenAccountDoesNotExist_shouldReturnFalse() {
        // Arrange
        String nonExistentAccountNumber = "9999999999999999";

        // Act
        boolean exists = accountRepository.existsByAccountNumber(nonExistentAccountNumber);

        // Assert
        assertFalse(exists);
    }
}
