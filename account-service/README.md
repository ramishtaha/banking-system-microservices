# Account Service Documentation

## Overview
The Account Service manages bank account lifecycle operations within the Banking System. It handles account creation, updates, balance management, and account status tracking while ensuring data consistency and regulatory compliance.

## 🏗️ Architecture

### Component Diagram
```
┌─────────────────┐
│   Controllers   │  ← REST API Layer
├─────────────────┤
│    Services     │  ← Business Logic Layer
├─────────────────┤
│  Repositories   │  ← Data Access Layer
├─────────────────┤
│   PostgreSQL    │  ← Data Storage
└─────────────────┘
```

### Package Structure
```
com.bankingsystem.accountservice/
├── controller/           # REST Controllers
│   ├── AccountController.java
│   └── AccountTypeController.java
├── service/             # Business Logic
│   ├── AccountService.java
│   ├── BalanceService.java
│   └── impl/
├── repository/          # Data Access
│   ├── AccountRepository.java
│   └── AccountTypeRepository.java
├── model/              # Domain Models
│   ├── Account.java
│   ├── AccountType.java
│   └── AccountStatus.java
├── dto/                # Data Transfer Objects
│   ├── AccountDto.java
│   ├── CreateAccountRequest.java
│   └── BalanceUpdateRequest.java
├── config/             # Configuration
│   ├── SecurityConfig.java
│   └── JpaConfig.java
├── exception/          # Custom Exceptions
│   ├── AccountNotFoundException.java
│   ├── InsufficientFundsException.java
│   └── InvalidAccountTypeException.java
└── client/             # External Service Clients
    └── UserServiceClient.java
```

## 🚀 Features

### Core Functionality
- ✅ Account Creation and Management
- ✅ Multiple Account Types (Checking, Savings, Credit, Investment)
- ✅ Balance Management and Tracking
- ✅ Account Status Management (Active, Suspended, Closed)
- ✅ Account Ownership and Authorization
- ✅ Transaction History Integration
- ✅ Compliance and Audit Trail

### Business Rules
- ✅ Account Number Generation (Unique, Sequential)
- ✅ Minimum Balance Requirements
- ✅ Maximum Account Limits per User
- ✅ Account Type-specific Rules
- ✅ Interest Calculation (Savings Accounts)
- ✅ Overdraft Protection (Checking Accounts)

## 📊 Database Schema

### Tables

#### accounts
```sql
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    account_type_id BIGINT NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    maximum_balance DECIMAL(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    branch_code VARCHAR(10),
    routing_number VARCHAR(15),
    nickname VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    last_transaction_date TIMESTAMP,
    
    CONSTRAINT fk_account_type 
        FOREIGN KEY (account_type_id) 
        REFERENCES account_types(id),
    CONSTRAINT chk_balance_positive 
        CHECK (balance >= 0),
    CONSTRAINT chk_available_balance 
        CHECK (available_balance >= 0),
    CONSTRAINT chk_status 
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED', 'PENDING'))
);
```

#### account_types
```sql
CREATE TABLE account_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    maximum_balance DECIMAL(15,2),
    interest_rate DECIMAL(5,4) DEFAULT 0.0000,
    overdraft_limit DECIMAL(15,2) DEFAULT 0.00,
    monthly_fee DECIMAL(10,2) DEFAULT 0.00,
    transaction_limit INTEGER,
    withdrawal_limit_daily DECIMAL(15,2),
    is_active BOOLEAN DEFAULT TRUE,
    requires_approval BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### account_balances_history
```sql
CREATE TABLE account_balances_history (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    previous_balance DECIMAL(15,2) NOT NULL,
    new_balance DECIMAL(15,2) NOT NULL,
    change_amount DECIMAL(15,2) NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    transaction_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_account_balance_history 
        FOREIGN KEY (account_id) 
        REFERENCES accounts(id),
    CONSTRAINT chk_change_type 
        CHECK (change_type IN ('CREDIT', 'DEBIT', 'INTEREST', 'FEE', 'ADJUSTMENT'))
);
```

### Indexes
```sql
-- Performance indexes
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_type_status ON accounts(account_type_id, status);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);

-- Balance history indexes
CREATE INDEX idx_balance_history_account_id ON account_balances_history(account_id);
CREATE INDEX idx_balance_history_created_at ON account_balances_history(created_at);

-- Account types indexes
CREATE INDEX idx_account_types_active ON account_types(is_active);
```

### Default Account Types
```sql
INSERT INTO account_types (name, description, minimum_balance, interest_rate, overdraft_limit, monthly_fee) VALUES
('CHECKING', 'Standard checking account', 0.00, 0.0000, 500.00, 0.00),
('SAVINGS', 'High-yield savings account', 100.00, 0.0150, 0.00, 0.00),
('CREDIT', 'Credit card account', 0.00, 0.0000, 5000.00, 0.00),
('INVESTMENT', 'Investment account', 1000.00, 0.0000, 0.00, 10.00),
('MONEY_MARKET', 'Money market account', 2500.00, 0.0200, 0.00, 5.00);
```

## 🔌 API Endpoints

### Account Management Endpoints

#### POST /api/accounts
Create a new account for a user.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "userId": 1,
  "accountTypeId": 1,
  "initialBalance": 1000.00,
  "nickname": "My Checking Account",
  "currency": "USD",
  "branchCode": "BR001"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "accountNumber": "1001234567890",
  "userId": 1,
  "accountType": {
    "id": 1,
    "name": "CHECKING",
    "description": "Standard checking account",
    "minimumBalance": 0.00,
    "interestRate": 0.0000
  },
  "balance": 1000.00,
  "availableBalance": 1000.00,
  "minimumBalance": 0.00,
  "status": "ACTIVE",
  "currency": "USD",
  "nickname": "My Checking Account",
  "isPrimary": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid account type or insufficient initial balance
- `403 Forbidden`: User not authorized to create account
- `409 Conflict`: Maximum account limit exceeded

#### GET /api/accounts/{accountId}
Get account details by ID.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `accountId`: Account ID

**Response (200 OK):**
```json
{
  "id": 1,
  "accountNumber": "1001234567890",
  "userId": 1,
  "accountType": {
    "id": 1,
    "name": "CHECKING",
    "description": "Standard checking account"
  },
  "balance": 1500.00,
  "availableBalance": 1500.00,
  "minimumBalance": 0.00,
  "status": "ACTIVE",
  "currency": "USD",
  "nickname": "My Checking Account",
  "isPrimary": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "lastTransactionDate": "2024-01-20T14:30:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Account not found
- `403 Forbidden`: User not authorized to view account

#### GET /api/accounts/user/{userId}
Get all accounts for a specific user.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `userId`: User ID

**Query Parameters:**
- `status`: Filter by account status (ACTIVE, SUSPENDED, CLOSED)
- `accountType`: Filter by account type name
- `includeBalance`: Include balance information (default: true)

**Example Request:**
```
GET /api/accounts/user/1?status=ACTIVE&accountType=CHECKING&includeBalance=true
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "accountNumber": "1001234567890",
    "accountType": {
      "id": 1,
      "name": "CHECKING"
    },
    "balance": 1500.00,
    "availableBalance": 1500.00,
    "status": "ACTIVE",
    "nickname": "My Checking Account",
    "isPrimary": true,
    "createdAt": "2024-01-15T10:30:00Z"
  },
  {
    "id": 2,
    "accountNumber": "1001234567891",
    "accountType": {
      "id": 2,
      "name": "SAVINGS"
    },
    "balance": 5000.00,
    "availableBalance": 5000.00,
    "status": "ACTIVE",
    "nickname": "Emergency Fund",
    "isPrimary": false,
    "createdAt": "2024-01-16T11:00:00Z"
  }
]
```

#### PUT /api/accounts/{accountId}
Update account information.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters:**
- `accountId`: Account ID

**Request Body:**
```json
{
  "nickname": "Updated Checking Account",
  "isPrimary": true
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "accountNumber": "1001234567890",
  "nickname": "Updated Checking Account",
  "isPrimary": true,
  "updatedAt": "2024-01-20T15:30:00Z"
}
```

#### PUT /api/accounts/{accountId}/status
Update account status.

**Headers:**
```
Authorization: Bearer <admin_access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "SUSPENDED",
  "reason": "Suspicious activity detected"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "SUSPENDED",
  "statusReason": "Suspicious activity detected",
  "updatedAt": "2024-01-20T16:00:00Z"
}
```

#### DELETE /api/accounts/{accountId}
Close an account (soft delete).

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `accountId`: Account ID

**Query Parameters:**
- `reason`: Reason for closing account

**Response (200 OK):**
```json
{
  "message": "Account successfully closed",
  "accountId": 1,
  "closedAt": "2024-01-20T17:00:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Account has outstanding balance
- `403 Forbidden`: User not authorized to close account
- `409 Conflict`: Account has pending transactions

### Balance Management Endpoints

#### GET /api/accounts/{accountId}/balance
Get current account balance.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "accountId": 1,
  "balance": 1500.00,
  "availableBalance": 1500.00,
  "minimumBalance": 0.00,
  "currency": "USD",
  "lastUpdated": "2024-01-20T14:30:00Z"
}
```

#### PUT /api/accounts/{accountId}/balance
Update account balance (Internal API for Transaction Service).

**Headers:**
```
Authorization: Bearer <service_access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": -500.00,
  "changeType": "DEBIT",
  "transactionReference": "TXN-123456789",
  "description": "ATM Withdrawal"
}
```

**Response (200 OK):**
```json
{
  "accountId": 1,
  "previousBalance": 1500.00,
  "newBalance": 1000.00,
  "changeAmount": -500.00,
  "availableBalance": 1000.00,
  "transactionReference": "TXN-123456789",
  "updatedAt": "2024-01-20T15:00:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Insufficient funds
- `422 Unprocessable Entity`: Balance update would violate business rules

#### GET /api/accounts/{accountId}/balance/history
Get balance change history.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `fromDate`: Start date (ISO format)
- `toDate`: End date (ISO format)
- `changeType`: Filter by change type (CREDIT, DEBIT, INTEREST, FEE)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "previousBalance": 1500.00,
      "newBalance": 1000.00,
      "changeAmount": -500.00,
      "changeType": "DEBIT",
      "transactionReference": "TXN-123456789",
      "createdAt": "2024-01-20T15:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Account Type Endpoints

#### GET /api/accounts/types
Get all available account types.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "CHECKING",
    "description": "Standard checking account",
    "minimumBalance": 0.00,
    "maximumBalance": null,
    "interestRate": 0.0000,
    "overdraftLimit": 500.00,
    "monthlyFee": 0.00,
    "transactionLimit": null,
    "withdrawalLimitDaily": 1000.00,
    "isActive": true,
    "requiresApproval": false
  },
  {
    "id": 2,
    "name": "SAVINGS",
    "description": "High-yield savings account",
    "minimumBalance": 100.00,
    "interestRate": 0.0150,
    "overdraftLimit": 0.00,
    "monthlyFee": 0.00,
    "transactionLimit": 6,
    "isActive": true,
    "requiresApproval": false
  }
]
```

#### GET /api/accounts/types/{typeId}
Get specific account type details.

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "CHECKING",
  "description": "Standard checking account with unlimited transactions",
  "minimumBalance": 0.00,
  "maximumBalance": null,
  "interestRate": 0.0000,
  "overdraftLimit": 500.00,
  "monthlyFee": 0.00,
  "transactionLimit": null,
  "withdrawalLimitDaily": 1000.00,
  "features": [
    "Unlimited transactions",
    "ATM access",
    "Online banking",
    "Overdraft protection"
  ],
  "isActive": true,
  "requiresApproval": false
}
```

### Administrative Endpoints

#### GET /api/accounts/admin/search
Search accounts (Admin only).

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Query Parameters:**
- `accountNumber`: Search by account number
- `userId`: Filter by user ID
- `status`: Filter by status
- `accountType`: Filter by account type
- `balanceMin`: Minimum balance filter
- `balanceMax`: Maximum balance filter
- `createdAfter`: Created after date
- `createdBefore`: Created before date
- `page`: Page number
- `size`: Page size
- `sort`: Sort field (balance, createdAt, etc.)
- `order`: Sort order (asc, desc)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "accountNumber": "1001234567890",
      "userId": 1,
      "userEmail": "john.doe@example.com",
      "accountType": "CHECKING",
      "balance": 1500.00,
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

#### GET /api/accounts/admin/statistics
Get account statistics (Admin only).

**Response (200 OK):**
```json
{
  "totalAccounts": 1250,
  "activeAccounts": 1180,
  "suspendedAccounts": 45,
  "closedAccounts": 25,
  "totalBalance": 15750000.00,
  "averageBalance": 12600.00,
  "accountsByType": {
    "CHECKING": 650,
    "SAVINGS": 400,
    "CREDIT": 150,
    "INVESTMENT": 50
  },
  "newAccountsThisMonth": 85,
  "balanceDistribution": {
    "0-1000": 320,
    "1000-5000": 450,
    "5000-10000": 280,
    "10000+": 200
  }
}
```

## 🔧 Configuration

### Application Properties

#### Development Profile (`application-dev.yml`)
```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/banking_accounts
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# Account Service Configuration
account:
  number:
    prefix: "10"
    length: 13
  balance:
    currency: "USD"
    scale: 2
  limits:
    max-accounts-per-user: 5
    daily-withdrawal-limit: 5000.00
  interest:
    calculation-frequency: "MONTHLY"
    compounding: "DAILY"

# User Service Integration
user-service:
  url: http://localhost:8081
  timeout: 5000

logging:
  level:
    com.bankingsystem.accountservice: DEBUG
    org.springframework.web: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

#### Production Profile (`application-prod.yml`)
```yaml
server:
  port: 8082

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://discovery-server:8761/eureka/}

account:
  number:
    prefix: ${ACCOUNT_NUMBER_PREFIX:10}
    length: ${ACCOUNT_NUMBER_LENGTH:13}
  balance:
    currency: ${DEFAULT_CURRENCY:USD}
  limits:
    max-accounts-per-user: ${MAX_ACCOUNTS_PER_USER:5}
    daily-withdrawal-limit: ${DAILY_WITHDRAWAL_LIMIT:5000.00}

user-service:
  url: ${USER_SERVICE_URL:http://user-service:8081}
  timeout: ${USER_SERVICE_TIMEOUT:5000}

logging:
  level:
    com.bankingsystem.accountservice: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /var/log/account-service.log

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

#### Test Profile (`application-test.yml`)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: true

eureka:
  client:
    enabled: false

account:
  number:
    prefix: "99"
    length: 10
  limits:
    max-accounts-per-user: 3

user-service:
  url: http://localhost:8081
  timeout: 1000

logging:
  level:
    com.bankingsystem.accountservice: DEBUG
```

### Environment Variables

#### Required Environment Variables (Production)
```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/banking_accounts
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secure_password

# Service Discovery
EUREKA_URL=http://discovery-server:8761/eureka/

# External Service URLs
USER_SERVICE_URL=http://user-service:8081

# Account Configuration
ACCOUNT_NUMBER_PREFIX=10
ACCOUNT_NUMBER_LENGTH=13
DEFAULT_CURRENCY=USD
MAX_ACCOUNTS_PER_USER=5
DAILY_WITHDRAWAL_LIMIT=5000.00

# Security
JWT_SECRET=super_secure_jwt_secret_key_minimum_256_bits
```

## 🧪 Testing

### Test Structure
```
src/test/java/com/bankingsystem/accountservice/
├── controller/
│   ├── AccountControllerTest.java
│   └── AccountTypeControllerTest.java
├── service/
│   ├── AccountServiceTest.java
│   └── BalanceServiceTest.java
├── repository/
│   ├── AccountRepositoryTest.java
│   └── AccountTypeRepositoryTest.java
├── integration/
│   ├── AccountServiceIntegrationTest.java
│   └── BalanceManagementIntegrationTest.java
├── client/
│   └── UserServiceClientTest.java
└── TestAccountServiceApplication.java
```

### Unit Tests

#### Service Layer Test Example
```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private AccountTypeRepository accountTypeRepository;
    
    @Mock
    private UserServiceClient userServiceClient;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    @DisplayName("Should create account successfully")
    void shouldCreateAccountSuccessfully() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .userId(1L)
            .accountTypeId(1L)
            .initialBalance(BigDecimal.valueOf(1000))
            .nickname("Test Account")
            .build();
            
        AccountType accountType = AccountType.builder()
            .id(1L)
            .name("CHECKING")
            .minimumBalance(BigDecimal.ZERO)
            .build();
            
        Account savedAccount = Account.builder()
            .id(1L)
            .accountNumber("1001234567890")
            .userId(1L)
            .accountType(accountType)
            .balance(BigDecimal.valueOf(1000))
            .status(AccountStatus.ACTIVE)
            .build();
            
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(accountTypeRepository.findById(1L)).thenReturn(Optional.of(accountType));
        when(accountRepository.countByUserIdAndStatus(1L, AccountStatus.ACTIVE)).thenReturn(0L);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // When
        AccountDto result = accountService.createAccount(request);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        verify(eventPublisher).publishEvent(any(AccountCreatedEvent.class));
    }
    
    @Test
    @DisplayName("Should throw exception when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .userId(999L)
            .build();
            
        when(userServiceClient.userExists(999L)).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(request))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User with ID 999 not found");
    }
    
    @Test
    @DisplayName("Should throw exception when maximum accounts exceeded")
    void shouldThrowExceptionWhenMaximumAccountsExceeded() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .userId(1L)
            .accountTypeId(1L)
            .build();
            
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(accountRepository.countByUserIdAndStatus(1L, AccountStatus.ACTIVE)).thenReturn(5L);
        
        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(request))
            .isInstanceOf(MaximumAccountsExceededException.class)
            .hasMessage("Maximum number of accounts (5) exceeded for user 1");
    }
}
```

#### Repository Layer Test Example
```java
@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should find accounts by user ID")
    void shouldFindAccountsByUserId() {
        // Given
        AccountType accountType = AccountType.builder()
            .name("CHECKING")
            .minimumBalance(BigDecimal.ZERO)
            .build();
        entityManager.persistAndFlush(accountType);
        
        Account account = Account.builder()
            .accountNumber("1001234567890")
            .userId(1L)
            .accountType(accountType)
            .balance(BigDecimal.valueOf(1000))
            .status(AccountStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(account);
        
        // When
        List<Account> accounts = accountRepository.findByUserId(1L);
        
        // Then
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getUserId()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should find account by account number")
    void shouldFindAccountByAccountNumber() {
        // Given
        AccountType accountType = AccountType.builder()
            .name("SAVINGS")
            .minimumBalance(BigDecimal.valueOf(100))
            .build();
        entityManager.persistAndFlush(accountType);
        
        Account account = Account.builder()
            .accountNumber("1001234567891")
            .userId(2L)
            .accountType(accountType)
            .balance(BigDecimal.valueOf(5000))
            .status(AccountStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(account);
        
        // When
        Optional<Account> found = accountRepository.findByAccountNumber("1001234567891");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getAccountType().getName()).isEqualTo("SAVINGS");
    }
    
    @Test
    @DisplayName("Should count active accounts by user")
    void shouldCountActiveAccountsByUser() {
        // Given
        AccountType accountType = AccountType.builder()
            .name("CHECKING")
            .minimumBalance(BigDecimal.ZERO)
            .build();
        entityManager.persistAndFlush(accountType);
        
        // Create multiple accounts for same user
        Account account1 = Account.builder()
            .accountNumber("1001234567890")
            .userId(1L)
            .accountType(accountType)
            .balance(BigDecimal.valueOf(1000))
            .status(AccountStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(account1);
        
        Account account2 = Account.builder()
            .accountNumber("1001234567891")
            .userId(1L)
            .accountType(accountType)
            .balance(BigDecimal.valueOf(2000))
            .status(AccountStatus.SUSPENDED)
            .build();
        entityManager.persistAndFlush(account2);
        
        // When
        Long activeCount = accountRepository.countByUserIdAndStatus(1L, AccountStatus.ACTIVE);
        
        // Then
        assertThat(activeCount).isEqualTo(1L);
    }
}
```

#### Controller Layer Test Example
```java
@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AccountService accountService;
    
    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("Should create account successfully")
    void shouldCreateAccountSuccessfully() throws Exception {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .userId(1L)
            .accountTypeId(1L)
            .initialBalance(BigDecimal.valueOf(1000))
            .nickname("Test Account")
            .build();
            
        AccountDto response = AccountDto.builder()
            .id(1L)
            .accountNumber("1001234567890")
            .userId(1L)
            .balance(BigDecimal.valueOf(1000))
            .status(AccountStatus.ACTIVE)
            .build();
            
        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1001234567890"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }
    
    @Test
    @DisplayName("Should return validation error for invalid request")
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .userId(null)  // Invalid: null user ID
            .accountTypeId(1L)
            .initialBalance(BigDecimal.valueOf(-100))  // Invalid: negative balance
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer valid-token"))
                .andExpected(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
    
    @Test
    @DisplayName("Should get account by ID")
    void shouldGetAccountById() throws Exception {
        // Given
        AccountDto account = AccountDto.builder()
            .id(1L)
            .accountNumber("1001234567890")
            .userId(1L)
            .balance(BigDecimal.valueOf(1500))
            .status(AccountStatus.ACTIVE)
            .build();
            
        when(accountService.getAccountById(1L)).thenReturn(account);
        
        // When & Then
        mockMvc.perform(get("/api/accounts/1")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1001234567890"))
                .andExpect(jsonPath("$.balance").value(1500.00));
    }
}
```

### Integration Tests

#### Full Service Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class AccountServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AccountTypeRepository accountTypeRepository;
    
    @MockBean
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        // Set up test data
        AccountType checkingType = AccountType.builder()
            .name("CHECKING")
            .description("Test checking account")
            .minimumBalance(BigDecimal.ZERO)
            .interestRate(BigDecimal.ZERO)
            .isActive(true)
            .build();
        accountTypeRepository.save(checkingType);
        
        when(userServiceClient.userExists(any(Long.class))).thenReturn(true);
    }

    @Test
    @DisplayName("Should complete account creation workflow")
    void shouldCompleteAccountCreationWorkflow() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .userId(1L)
            .accountTypeId(1L)
            .initialBalance(BigDecimal.valueOf(1000))
            .nickname("Integration Test Account")
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer test-token");
        HttpEntity<CreateAccountRequest> entity = new HttpEntity<>(request, headers);
        
        // When - Create account
        ResponseEntity<AccountDto> response = restTemplate.postForEntity(
            "/api/accounts", entity, AccountDto.class);
        
        // Then - Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        
        // Verify account saved in database
        Optional<Account> savedAccount = accountRepository.findById(response.getBody().getId());
        assertThat(savedAccount).isPresent();
        assertThat(savedAccount.get().getNickname()).isEqualTo("Integration Test Account");
        
        // Test getting the account
        ResponseEntity<AccountDto> getResponse = restTemplate.exchange(
            "/api/accounts/" + response.getBody().getId(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            AccountDto.class);
            
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getId()).isEqualTo(response.getBody().getId());
    }
}
```

### Running Tests
```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AccountServiceTest

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Generate test report
mvn surefire-report:report-only
```

## 🔄 Events

### Published Events

#### AccountCreatedEvent
```java
@Data
@AllArgsConstructor
public class AccountCreatedEvent {
    private Long accountId;
    private String accountNumber;
    private Long userId;
    private String accountType;
    private BigDecimal initialBalance;
    private LocalDateTime createdAt;
}
```

#### AccountUpdatedEvent
```java
@Data
@AllArgsConstructor
public class AccountUpdatedEvent {
    private Long accountId;
    private String accountNumber;
    private Long userId;
    private Map<String, Object> changedFields;
    private LocalDateTime updatedAt;
}
```

#### BalanceChangedEvent
```java
@Data
@AllArgsConstructor
public class BalanceChangedEvent {
    private Long accountId;
    private String accountNumber;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private BigDecimal changeAmount;
    private String changeType;
    private String transactionReference;
    private LocalDateTime changedAt;
}
```

#### AccountStatusChangedEvent
```java
@Data
@AllArgsConstructor
public class AccountStatusChangedEvent {
    private Long accountId;
    private String accountNumber;
    private AccountStatus oldStatus;
    private AccountStatus newStatus;
    private String reason;
    private LocalDateTime changedAt;
}
```

### Event Publishing
```java
@Service
public class AccountServiceImpl implements AccountService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public AccountDto createAccount(CreateAccountRequest request) {
        Account account = accountRepository.save(newAccount);
        
        // Publish event
        eventPublisher.publishEvent(new AccountCreatedEvent(
            account.getId(),
            account.getAccountNumber(),
            account.getUserId(),
            account.getAccountType().getName(),
            account.getBalance(),
            account.getCreatedAt()
        ));
        
        return AccountMapper.toDto(account);
    }
}
```

## 🔍 Monitoring & Metrics

### Custom Metrics
```java
@Component
public class AccountMetrics {
    
    private final Counter accountCreations = Counter.builder("account.creations")
        .description("Total account creations")
        .tag("type", "all")
        .register(Metrics.globalRegistry);
        
    private final Timer balanceUpdateDuration = Timer.builder("account.balance.update.duration")
        .description("Balance update duration")
        .register(Metrics.globalRegistry);
        
    private final Gauge totalBalance = Gauge.builder("account.total.balance")
        .description("Total balance across all accounts")
        .register(Metrics.globalRegistry, this, AccountMetrics::getTotalBalance);
        
    private final Gauge activeAccountsCount = Gauge.builder("account.active.count")
        .description("Active accounts count")
        .register(Metrics.globalRegistry, this, AccountMetrics::getActiveAccountCount);
        
    public void recordAccountCreation(String accountType) {
        accountCreations.increment(Tags.of("type", accountType));
    }
    
    public Timer.Sample startBalanceUpdateTimer() {
        return Timer.start(Metrics.globalRegistry);
    }
    
    private double getTotalBalance() {
        return accountRepository.sumBalanceByStatus(AccountStatus.ACTIVE)
            .orElse(BigDecimal.ZERO).doubleValue();
    }
    
    private double getActiveAccountCount() {
        return accountRepository.countByStatus(AccountStatus.ACTIVE);
    }
}
```

### Health Checks
```java
@Component
public class AccountServiceHealthIndicator implements HealthIndicator {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public Health health() {
        try {
            // Check database connectivity
            long accountCount = accountRepository.count();
            
            // Check external service connectivity
            boolean userServiceAvailable = userServiceClient.healthCheck();
            
            if (userServiceAvailable) {
                return Health.up()
                    .withDetail("accountCount", accountCount)
                    .withDetail("userServiceStatus", "UP")
                    .withDetail("status", "All systems operational")
                    .build();
            } else {
                return Health.down()
                    .withDetail("accountCount", accountCount)
                    .withDetail("userServiceStatus", "DOWN")
                    .withDetail("error", "User service unavailable")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 🔒 Security Considerations

### Account Access Control
- **Account Ownership**: Users can only access their own accounts
- **Admin Access**: Administrative users can access all accounts with audit logging
- **Service-to-Service**: Internal APIs protected with service tokens
- **Rate Limiting**: API rate limiting to prevent abuse

### Data Protection
- **Sensitive Data**: Account numbers and balances encrypted at rest
- **Audit Trail**: Complete audit log of all account operations
- **PCI Compliance**: Adherence to payment card industry standards
- **Data Masking**: Account numbers masked in logs and non-secure contexts

### API Security
```java
@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasRole('USER')")
public class AccountController {

    @GetMapping("/{accountId}")
    @PreAuthorize("@accountService.isAccountOwner(#accountId, authentication.name) or hasRole('ADMIN')")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long accountId) {
        // Implementation
    }
    
    @PutMapping("/{accountId}/balance")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<BalanceUpdateResponse> updateBalance(
            @PathVariable Long accountId,
            @RequestBody BalanceUpdateRequest request) {
        // Internal service API
    }
}
```

## 🚀 Deployment

### Docker Configuration

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY target/account-service-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### Docker Compose
```yaml
services:
  account-service:
    build: .
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DATABASE_URL=jdbc:postgresql://postgres:5432/banking_accounts
      - USER_SERVICE_URL=http://user-service:8081
      - EUREKA_URL=http://discovery-server:8761/eureka/
    depends_on:
      - postgres
      - discovery-server
      - user-service
    networks:
      - banking-network
```

### Kubernetes Deployment

#### Deployment Manifest
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: account-service
  namespace: banking-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: account-service
  template:
    metadata:
      labels:
        app: account-service
    spec:
      containers:
      - name: account-service
        image: banking-system/account-service:1.0.0
        ports:
        - containerPort: 8082
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: accounts-url
        - name: USER_SERVICE_URL
          value: "http://user-service:8081"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8082
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

## 📈 Performance Optimization

### Database Optimization
- **Connection Pooling**: Optimized HikariCP settings
- **Query Optimization**: Efficient queries with proper indexing
- **Caching**: Redis for frequently accessed account data
- **Partitioning**: Table partitioning for large datasets

### Application Optimization
- **Async Processing**: Non-blocking balance updates
- **Caching**: Spring Cache for account types and static data
- **Bulk Operations**: Batch processing for multiple accounts
- **Memory Management**: Optimal JVM heap and GC settings

## 🐛 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check database connectivity
docker exec -it postgres psql -U postgres -d banking_accounts

# Verify connection pool
curl http://localhost:8082/actuator/metrics/hikaricp.connections.active
```

#### User Service Integration Issues
```bash
# Check user service connectivity
curl http://localhost:8081/actuator/health

# Verify service discovery
curl http://localhost:8761/eureka/apps/USER-SERVICE
```

#### Balance Inconsistency Issues
```bash
# Check balance history
SELECT * FROM account_balances_history WHERE account_id = 1 ORDER BY created_at DESC LIMIT 10;

# Verify account balance
SELECT id, account_number, balance, available_balance FROM accounts WHERE id = 1;
```

### Log Analysis
```bash
# Application logs
docker logs account-service | grep -i "balance"

# Database query logs
docker logs postgres | grep -i "statement"

# Error logs
docker logs account-service | grep ERROR
```

## 📚 Additional Resources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Best Practices](https://wiki.postgresql.org/wiki/Don%27t_Do_This)
- [Banking System Regulations](https://www.fdic.gov/regulations/)
- [PCI DSS Compliance](https://www.pcisecuritystandards.org/)

---

**Account Service - Banking System v1.0**
