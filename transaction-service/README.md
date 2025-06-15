# Transaction Service Documentation

## Overview
The Transaction Service is a critical component of the Banking System responsible for processing financial transactions between accounts. It ensures data consistency, provides transaction history, and implements security measures for all money movement operations.

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
com.bankingsystem.transactionservice/
├── controller/           # REST Controllers
│   ├── TransactionController.java
│   └── TransferController.java
├── service/             # Business Logic
│   ├── TransactionService.java
│   ├── TransferService.java
│   └── impl/
├── repository/          # Data Access
│   ├── TransactionRepository.java
│   └── TransactionTypeRepository.java
├── model/              # Domain Models
│   ├── Transaction.java
│   ├── TransactionType.java
│   └── TransactionStatus.java
├── dto/                # Data Transfer Objects
│   ├── TransactionDto.java
│   ├── TransferRequest.java
│   └── TransactionResponse.java
├── config/             # Configuration
│   ├── SecurityConfig.java
│   └── KafkaConfig.java
├── exception/          # Custom Exceptions
│   ├── InsufficientFundsException.java
│   └── InvalidTransactionException.java
└── client/             # External Service Clients
    └── AccountServiceClient.java
```

## 🚀 Features

### Core Functionality
- ✅ Process various transaction types (Deposits, Withdrawals, Transfers)
- ✅ Account balance verification and updates
- ✅ Transaction history and audit trails
- ✅ Multi-currency support with exchange rate conversion
- ✅ Transaction status tracking and notifications
- ✅ Scheduled and recurring transactions
- ✅ Transaction fees calculation
- ✅ Fraud detection and prevention

### Transaction Types
- ✅ **Transfers**: Between internal accounts
- ✅ **Deposits**: Cash and check deposits
- ✅ **Withdrawals**: ATM and branch withdrawals
- ✅ **Bill Payments**: Recurring and one-time
- ✅ **External Transfers**: ACH and wire transfers
- ✅ **Card Payments**: Debit and credit card transactions

## 📊 Database Schema

### Tables

#### transactions
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_reference VARCHAR(50) UNIQUE NOT NULL,
    transaction_type_id BIGINT NOT NULL,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate DECIMAL(10,6),
    fee_amount DECIMAL(10,2),
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    status_reason TEXT,
    description TEXT,
    reference_number VARCHAR(100),
    user_id BIGINT,
    scheduled_date DATE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transaction_type 
        FOREIGN KEY (transaction_type_id) 
        REFERENCES transaction_types(id),
    CONSTRAINT chk_amount_positive 
        CHECK (amount > 0),
    CONSTRAINT chk_status 
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'PROCESSING'))
);
```

#### transaction_types
```sql
CREATE TABLE transaction_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    is_credit BOOLEAN NOT NULL,
    default_fee DECIMAL(10,2) DEFAULT 0.00,
    daily_limit DECIMAL(15,2),
    requires_approval BOOLEAN DEFAULT FALSE,
    is_internal BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### recurring_transactions
```sql
CREATE TABLE recurring_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_account_id BIGINT NOT NULL,
    destination_account_id BIGINT NOT NULL,
    transaction_type_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    frequency VARCHAR(20) NOT NULL,
    next_execution_date DATE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    day_of_month INTEGER,
    day_of_week INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_recurring_transaction_type 
        FOREIGN KEY (transaction_type_id) 
        REFERENCES transaction_types(id),
    CONSTRAINT chk_frequency 
        CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY'))
);
```

#### transaction_logs
```sql
CREATE TABLE transaction_logs (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    message TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transaction_log 
        FOREIGN KEY (transaction_id) 
        REFERENCES transactions(id)
);
```

### Indexes
```sql
-- Performance indexes
CREATE INDEX idx_transactions_reference ON transactions(transaction_reference);
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_destination_account ON transactions(destination_account_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_scheduled_date ON transactions(scheduled_date);

-- For scheduled transactions
CREATE INDEX idx_recurring_next_execution ON recurring_transactions(next_execution_date, is_active);

-- For analytics
CREATE INDEX idx_transactions_type_date ON transactions(transaction_type_id, created_at);
```

### Default Transaction Types
```sql
INSERT INTO transaction_types (name, description, is_credit, default_fee, is_internal) VALUES
('DEPOSIT', 'Cash or check deposit to account', TRUE, 0.00, TRUE),
('WITHDRAWAL', 'Cash withdrawal from account', FALSE, 0.00, TRUE),
('TRANSFER_INTERNAL', 'Transfer between internal accounts', NULL, 0.00, TRUE),
('TRANSFER_EXTERNAL', 'Transfer to external account', FALSE, 5.00, FALSE),
('BILL_PAYMENT', 'Payment to service provider', FALSE, 0.00, FALSE),
('CARD_PAYMENT', 'Debit or credit card payment', FALSE, 0.00, TRUE),
('INTEREST_CREDIT', 'Interest earned on account', TRUE, 0.00, TRUE),
('FEE', 'Service or transaction fee', FALSE, 0.00, TRUE);
```

## 🔌 API Endpoints

### Transaction Management Endpoints

#### POST /api/transactions
Create a new transaction (deposit or withdrawal).

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body (Deposit):**
```json
{
  "transactionType": "DEPOSIT",
  "destinationAccountId": 1,
  "amount": 1000.00,
  "currency": "USD",
  "description": "Initial deposit",
  "referenceNumber": "DEP12345"
}
```

**Request Body (Withdrawal):**
```json
{
  "transactionType": "WITHDRAWAL",
  "sourceAccountId": 1,
  "amount": 500.00,
  "currency": "USD",
  "description": "ATM withdrawal",
  "referenceNumber": "ATM12345"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "transactionReference": "TXN1234567890",
  "transactionType": "DEPOSIT",
  "sourceAccount": null,
  "destinationAccount": {
    "id": 1,
    "accountNumber": "1001234567890"
  },
  "amount": 1000.00,
  "currency": "USD",
  "feeAmount": 0.00,
  "totalAmount": 1000.00,
  "status": "COMPLETED",
  "description": "Initial deposit",
  "referenceNumber": "DEP12345",
  "createdAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:30:05Z"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid transaction details
- `404 Not Found`: Account not found
- `422 Unprocessable Entity`: Insufficient funds or business rule violation

#### POST /api/transactions/transfer
Transfer funds between accounts.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 500.00,
  "currency": "USD",
  "description": "Monthly savings transfer"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "transactionReference": "TXN1234567891",
  "transactionType": "TRANSFER_INTERNAL",
  "sourceAccount": {
    "id": 1,
    "accountNumber": "1001234567890"
  },
  "destinationAccount": {
    "id": 2,
    "accountNumber": "1001234567891"
  },
  "amount": 500.00,
  "currency": "USD",
  "feeAmount": 0.00,
  "totalAmount": 500.00,
  "status": "COMPLETED",
  "description": "Monthly savings transfer",
  "createdAt": "2024-01-15T11:00:00Z",
  "completedAt": "2024-01-15T11:00:05Z"
}
```

#### POST /api/transactions/external-transfer
Transfer funds to an external account.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "sourceAccountId": 1,
  "recipientName": "Jane Smith",
  "recipientBankName": "Other Bank",
  "recipientAccountNumber": "987654321",
  "recipientRoutingNumber": "012345678",
  "amount": 1000.00,
  "currency": "USD",
  "description": "Rent payment",
  "transferMethod": "ACH"
}
```

**Response (202 Accepted):**
```json
{
  "id": 3,
  "transactionReference": "TXN1234567892",
  "transactionType": "TRANSFER_EXTERNAL",
  "sourceAccount": {
    "id": 1,
    "accountNumber": "1001234567890"
  },
  "amount": 1000.00,
  "feeAmount": 5.00,
  "totalAmount": 1005.00,
  "currency": "USD",
  "status": "PENDING",
  "description": "Rent payment",
  "recipientName": "Jane Smith",
  "recipientAccountNumber": "xxxx4321",
  "estimatedCompletionDate": "2024-01-18T00:00:00Z",
  "createdAt": "2024-01-15T12:00:00Z"
}
```

#### GET /api/transactions/{transactionId}
Get transaction details by ID.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `transactionId`: Transaction ID

**Response (200 OK):**
```json
{
  "id": 1,
  "transactionReference": "TXN1234567890",
  "transactionType": "DEPOSIT",
  "sourceAccount": null,
  "destinationAccount": {
    "id": 1,
    "accountNumber": "1001234567890",
    "nickname": "My Checking Account"
  },
  "amount": 1000.00,
  "currency": "USD",
  "feeAmount": 0.00,
  "totalAmount": 1000.00,
  "status": "COMPLETED",
  "description": "Initial deposit",
  "referenceNumber": "DEP12345",
  "createdAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:30:05Z",
  "createdBy": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

#### GET /api/transactions/reference/{reference}
Get transaction by reference number.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `reference`: Transaction reference number

**Response (200 OK):**
```json
{
  "id": 1,
  "transactionReference": "TXN1234567890",
  "transactionType": "DEPOSIT",
  "amount": 1000.00,
  "status": "COMPLETED",
  "description": "Initial deposit",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### GET /api/transactions/account/{accountId}
Get transactions for a specific account.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `accountId`: Account ID

**Query Parameters:**
- `startDate`: Filter by start date (ISO format)
- `endDate`: Filter by end date (ISO format)
- `type`: Filter by transaction type
- `status`: Filter by status
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field (createdAt, amount, etc.)
- `order`: Sort order (asc, desc)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 2,
      "transactionReference": "TXN1234567891",
      "transactionType": "TRANSFER_INTERNAL",
      "amount": 500.00,
      "currency": "USD",
      "direction": "OUTGOING",
      "status": "COMPLETED",
      "description": "Monthly savings transfer",
      "createdAt": "2024-01-15T11:00:00Z"
    },
    {
      "id": 1,
      "transactionReference": "TXN1234567890",
      "transactionType": "DEPOSIT",
      "amount": 1000.00,
      "currency": "USD",
      "direction": "INCOMING",
      "status": "COMPLETED",
      "description": "Initial deposit",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### GET /api/transactions/user/{userId}
Get all transactions for a user across all accounts.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `userId`: User ID

**Query Parameters:**
- Similar to `/api/transactions/account/{accountId}`

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 3,
      "transactionReference": "TXN1234567892",
      "transactionType": "TRANSFER_EXTERNAL",
      "sourceAccount": {
        "id": 1,
        "accountNumber": "1001234567890"
      },
      "amount": 1000.00,
      "currency": "USD",
      "status": "PENDING",
      "description": "Rent payment",
      "createdAt": "2024-01-15T12:00:00Z"
    },
    // ...more transactions
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

### Recurring Transaction Endpoints

#### POST /api/transactions/recurring
Create a recurring transaction.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 200.00,
  "description": "Monthly savings",
  "frequency": "MONTHLY",
  "dayOfMonth": 15,
  "startDate": "2024-02-15",
  "endDate": "2024-12-15"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "sourceAccount": {
    "id": 1,
    "accountNumber": "1001234567890"
  },
  "destinationAccount": {
    "id": 2,
    "accountNumber": "1001234567891"
  },
  "amount": 200.00,
  "description": "Monthly savings",
  "frequency": "MONTHLY",
  "nextExecutionDate": "2024-02-15",
  "startDate": "2024-02-15",
  "endDate": "2024-12-15",
  "dayOfMonth": 15,
  "isActive": true,
  "createdAt": "2024-01-15T14:00:00Z"
}
```

#### GET /api/transactions/recurring/user/{userId}
Get all recurring transactions for a user.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "sourceAccount": {
      "id": 1,
      "accountNumber": "1001234567890"
    },
    "destinationAccount": {
      "id": 2,
      "accountNumber": "1001234567891"
    },
    "amount": 200.00,
    "description": "Monthly savings",
    "frequency": "MONTHLY",
    "nextExecutionDate": "2024-02-15",
    "endDate": "2024-12-15",
    "isActive": true
  }
]
```

#### PUT /api/transactions/recurring/{recurringTransactionId}
Update a recurring transaction.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": 250.00,
  "isActive": true,
  "endDate": "2025-01-15"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "amount": 250.00,
  "isActive": true,
  "endDate": "2025-01-15",
  "updatedAt": "2024-01-15T15:00:00Z"
}
```

#### DELETE /api/transactions/recurring/{recurringTransactionId}
Cancel a recurring transaction.

**Response (200 OK):**
```json
{
  "message": "Recurring transaction cancelled successfully",
  "id": 1
}
```

### Administrative Endpoints

#### GET /api/transactions/admin/search
Search transactions (Admin only).

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Query Parameters:**
- `accountId`: Filter by account ID
- `userId`: Filter by user ID
- `reference`: Search by transaction reference
- `type`: Filter by transaction type
- `status`: Filter by status
- `amountMin`: Minimum amount filter
- `amountMax`: Maximum amount filter
- `startDate`: Created after date
- `endDate`: Created before date
- `page`, `size`, `sort`, `order`: Pagination and sorting

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 3,
      "transactionReference": "TXN1234567892",
      "transactionType": "TRANSFER_EXTERNAL",
      "userId": 1,
      "userName": "John Doe",
      "sourceAccountId": 1,
      "sourceAccountNumber": "1001234567890",
      "amount": 1000.00,
      "status": "PENDING",
      "createdAt": "2024-01-15T12:00:00Z"
    }
    // ...more transactions
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

#### GET /api/transactions/admin/statistics
Get transaction statistics (Admin only).

**Response (200 OK):**
```json
{
  "totalTransactions": 1250,
  "pendingTransactions": 45,
  "completedTransactions": 1180,
  "failedTransactions": 25,
  "totalProcessed": {
    "USD": 750000.00,
    "EUR": 15000.00
  },
  "averageTransactionAmount": {
    "USD": 600.00,
    "EUR": 750.00
  },
  "transactionsByType": {
    "DEPOSIT": 350,
    "WITHDRAWAL": 400,
    "TRANSFER_INTERNAL": 320,
    "TRANSFER_EXTERNAL": 180
  },
  "transactionsPerDay": [
    {
      "date": "2024-01-14",
      "count": 78
    },
    {
      "date": "2024-01-15",
      "count": 85
    }
  ]
}
```

## 🔧 Configuration

### Application Properties

#### Development Profile (`application-dev.yml`)
```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/banking_transactions
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

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: transaction-service
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# Transaction Service Configuration
transaction:
  reference:
    prefix: "TXN"
    length: 16
  limits:
    max-amount:
      TRANSFER_INTERNAL: 10000.00
      TRANSFER_EXTERNAL: 5000.00
      WITHDRAWAL: 2000.00
    daily-limit:
      TRANSFER: 20000.00
      WITHDRAWAL: 5000.00
  fees:
    TRANSFER_EXTERNAL:
      WIRE: 25.00
      ACH: 5.00
    WITHDRAWAL:
      ATM_EXTERNAL: 2.50

# External Service Integration
account-service:
  url: http://localhost:8082
  timeout: 5000

notification-service:
  url: http://localhost:8084

logging:
  level:
    com.bankingsystem.transactionservice: DEBUG
    org.springframework.web: DEBUG
    org.springframework.kafka: INFO

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
  port: 8083

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

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    consumer:
      group-id: transaction-service
      auto-offset-reset: earliest
    producer:
      acks: all
      retries: 3

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://discovery-server:8761/eureka/}

transaction:
  limits:
    max-amount:
      TRANSFER_INTERNAL: ${MAX_INTERNAL_TRANSFER:10000.00}
      TRANSFER_EXTERNAL: ${MAX_EXTERNAL_TRANSFER:5000.00}

account-service:
  url: ${ACCOUNT_SERVICE_URL:http://account-service:8082}
  timeout: ${ACCOUNT_SERVICE_TIMEOUT:5000}

notification-service:
  url: ${NOTIFICATION_SERVICE_URL:http://notification-service:8084}

logging:
  level:
    com.bankingsystem.transactionservice: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /var/log/transaction-service.log

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

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: transaction-service-test
      auto-start-up: false

eureka:
  client:
    enabled: false

transaction:
  limits:
    max-amount:
      TRANSFER_INTERNAL: 1000.00
      TRANSFER_EXTERNAL: 500.00
    daily-limit:
      TRANSFER: 2000.00

account-service:
  url: http://localhost:8082
  timeout: 1000

notification-service:
  url: http://localhost:8084

logging:
  level:
    com.bankingsystem.transactionservice: DEBUG
```

### Environment Variables

#### Required Environment Variables (Production)
```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/banking_transactions
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secure_password

# Service Discovery
EUREKA_URL=http://discovery-server:8761/eureka/

# Message Queue
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# External Service URLs
ACCOUNT_SERVICE_URL=http://account-service:8082
NOTIFICATION_SERVICE_URL=http://notification-service:8084

# Transaction Limits
MAX_INTERNAL_TRANSFER=10000.00
MAX_EXTERNAL_TRANSFER=5000.00
DAILY_WITHDRAWAL_LIMIT=5000.00

# Security
JWT_SECRET=super_secure_jwt_secret_key_minimum_256_bits
```

## 🧪 Testing

### Test Structure
```
src/test/java/com/bankingsystem/transactionservice/
├── controller/
│   ├── TransactionControllerTest.java
│   └── TransferControllerTest.java
├── service/
│   ├── TransactionServiceTest.java
│   └── TransferServiceTest.java
├── repository/
│   ├── TransactionRepositoryTest.java
│   └── RecurringTransactionRepositoryTest.java
├── integration/
│   ├── TransactionFlowIntegrationTest.java
│   └── RecurringTransactionIntegrationTest.java
├── client/
│   ├── AccountServiceClientTest.java
│   └── NotificationServiceClientTest.java
└── TestTransactionServiceApplication.java
```

### Unit Tests

#### Service Layer Test Example
```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private TransactionTypeRepository transactionTypeRepository;
    
    @Mock
    private AccountServiceClient accountServiceClient;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("Should create deposit transaction successfully")
    void shouldCreateDepositTransactionSuccessfully() {
        // Given
        TransactionRequest request = TransactionRequest.builder()
            .transactionType("DEPOSIT")
            .destinationAccountId(1L)
            .amount(BigDecimal.valueOf(1000))
            .description("Test deposit")
            .build();
            
        TransactionType depositType = TransactionType.builder()
            .id(1L)
            .name("DEPOSIT")
            .isCredit(true)
            .build();
            
        AccountDto accountDto = AccountDto.builder()
            .id(1L)
            .accountNumber("1001234567890")
            .userId(1L)
            .build();
            
        Transaction savedTransaction = Transaction.builder()
            .id(1L)
            .transactionReference("TXN1234567890")
            .transactionType(depositType)
            .destinationAccountId(1L)
            .amount(BigDecimal.valueOf(1000))
            .status(TransactionStatus.COMPLETED)
            .build();
            
        when(transactionTypeRepository.findByName("DEPOSIT"))
            .thenReturn(Optional.of(depositType));
        when(accountServiceClient.getAccount(1L))
            .thenReturn(accountDto);
        when(accountServiceClient.updateBalance(eq(1L), any()))
            .thenReturn(new BalanceUpdateResponse());
        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(savedTransaction);
        
        // When
        TransactionDto result = transactionService.createTransaction(request);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        verify(eventPublisher).publishEvent(any(TransactionCreatedEvent.class));
        verify(accountServiceClient).updateBalance(eq(1L), any());
    }
    
    @Test
    @DisplayName("Should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        // Given
        TransactionRequest request = TransactionRequest.builder()
            .transactionType("WITHDRAWAL")
            .sourceAccountId(999L)
            .amount(BigDecimal.valueOf(500))
            .build();
            
        when(accountServiceClient.getAccount(999L))
            .thenThrow(new AccountNotFoundException("Account not found"));
        
        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessage("Account not found");
    }
    
    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void shouldThrowExceptionWhenInsufficientFunds() {
        // Given
        TransactionRequest request = TransactionRequest.builder()
            .transactionType("WITHDRAWAL")
            .sourceAccountId(1L)
            .amount(BigDecimal.valueOf(5000))
            .build();
            
        TransactionType withdrawalType = TransactionType.builder()
            .id(2L)
            .name("WITHDRAWAL")
            .isCredit(false)
            .build();
            
        AccountDto accountDto = AccountDto.builder()
            .id(1L)
            .accountNumber("1001234567890")
            .userId(1L)
            .balance(BigDecimal.valueOf(1000))
            .build();
            
        when(transactionTypeRepository.findByName("WITHDRAWAL"))
            .thenReturn(Optional.of(withdrawalType));
        when(accountServiceClient.getAccount(1L))
            .thenReturn(accountDto);
        
        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
            .isInstanceOf(InsufficientFundsException.class)
            .hasMessage("Insufficient funds for withdrawal");
    }
}
```

#### Repository Layer Test Example
```java
@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Test
    @DisplayName("Should find transactions by account ID")
    void shouldFindTransactionsByAccountId() {
        // Given
        TransactionType depositType = TransactionType.builder()
            .name("DEPOSIT")
            .isCredit(true)
            .build();
        entityManager.persist(depositType);
        
        Transaction transaction = Transaction.builder()
            .transactionReference("TXN1234567890")
            .transactionType(depositType)
            .destinationAccountId(1L)
            .amount(BigDecimal.valueOf(1000))
            .currency("USD")
            .totalAmount(BigDecimal.valueOf(1000))
            .status(TransactionStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persistAndFlush(transaction);
        
        // When
        List<Transaction> transactions = transactionRepository
            .findBySourceAccountIdOrDestinationAccountId(null, 1L);
        
        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getDestinationAccountId()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should find transactions by transaction reference")
    void shouldFindTransactionsByReference() {
        // Given
        TransactionType withdrawalType = TransactionType.builder()
            .name("WITHDRAWAL")
            .isCredit(false)
            .build();
        entityManager.persist(withdrawalType);
        
        Transaction transaction = Transaction.builder()
            .transactionReference("TXN9876543210")
            .transactionType(withdrawalType)
            .sourceAccountId(1L)
            .amount(BigDecimal.valueOf(500))
            .currency("USD")
            .totalAmount(BigDecimal.valueOf(500))
            .status(TransactionStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persistAndFlush(transaction);
        
        // When
        Optional<Transaction> found = transactionRepository
            .findByTransactionReference("TXN9876543210");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTransactionType().getName()).isEqualTo("WITHDRAWAL");
    }
    
    @Test
    @DisplayName("Should find transactions by user ID")
    void shouldFindTransactionsByUserId() {
        // Given
        TransactionType transferType = TransactionType.builder()
            .name("TRANSFER_INTERNAL")
            .build();
        entityManager.persist(transferType);
        
        Transaction transaction = Transaction.builder()
            .transactionReference("TXN1122334455")
            .transactionType(transferType)
            .sourceAccountId(1L)
            .destinationAccountId(2L)
            .amount(BigDecimal.valueOf(750))
            .currency("USD")
            .totalAmount(BigDecimal.valueOf(750))
            .status(TransactionStatus.COMPLETED)
            .userId(1L)
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persistAndFlush(transaction);
        
        // When
        List<Transaction> transactions = transactionRepository.findByUserId(1L);
        
        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(750));
    }
}
```

#### Controller Layer Test Example
```java
@WebMvcTest(TransactionController.class)
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TransactionService transactionService;
    
    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() throws Exception {
        // Given
        TransactionRequest request = TransactionRequest.builder()
            .transactionType("DEPOSIT")
            .destinationAccountId(1L)
            .amount(BigDecimal.valueOf(1000))
            .description("Test deposit")
            .build();
            
        TransactionDto response = TransactionDto.builder()
            .id(1L)
            .transactionReference("TXN1234567890")
            .transactionType("DEPOSIT")
            .amount(BigDecimal.valueOf(1000))
            .status(TransactionStatus.COMPLETED)
            .build();
            
        when(transactionService.createTransaction(any(TransactionRequest.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.transactionReference").value("TXN1234567890"))
                .andExpect(jsonPath("$.amount").value(1000));
    }
    
    @Test
    @DisplayName("Should return validation error for invalid request")
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        // Given
        TransactionRequest request = TransactionRequest.builder()
            .transactionType("WITHDRAWAL")
            .sourceAccountId(null)  // Invalid: required field
            .amount(BigDecimal.valueOf(-100))  // Invalid: negative amount
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
    
    @Test
    @DisplayName("Should get transaction by ID")
    void shouldGetTransactionById() throws Exception {
        // Given
        TransactionDto transaction = TransactionDto.builder()
            .id(1L)
            .transactionReference("TXN1234567890")
            .transactionType("DEPOSIT")
            .destinationAccount(new AccountSummaryDto(1L, "1001234567890"))
            .amount(BigDecimal.valueOf(1000))
            .status(TransactionStatus.COMPLETED)
            .build();
            
        when(transactionService.getTransactionById(1L)).thenReturn(transaction);
        
        // When & Then
        mockMvc.perform(get("/api/transactions/1")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.transactionReference").value("TXN1234567890"))
                .andExpect(jsonPath("$.amount").value(1000));
    }
}
```

### Integration Tests

#### Full Service Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class TransactionFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransactionTypeRepository transactionTypeRepository;
    
    @MockBean
    private AccountServiceClient accountServiceClient;

    @BeforeEach
    void setUp() {
        // Set up transaction types
        if (!transactionTypeRepository.findByName("DEPOSIT").isPresent()) {
            TransactionType deposit = TransactionType.builder()
                .name("DEPOSIT")
                .description("Test deposit transaction")
                .isCredit(true)
                .isActive(true)
                .build();
            transactionTypeRepository.save(deposit);
        }
        
        if (!transactionTypeRepository.findByName("WITHDRAWAL").isPresent()) {
            TransactionType withdrawal = TransactionType.builder()
                .name("WITHDRAWAL")
                .description("Test withdrawal transaction")
                .isCredit(false)
                .isActive(true)
                .build();
            transactionTypeRepository.save(withdrawal);
        }
        
        // Mock account service calls
        AccountDto accountDto = AccountDto.builder()
            .id(1L)
            .accountNumber("1001234567890")
            .userId(1L)
            .balance(BigDecimal.valueOf(5000))
            .build();
            
        when(accountServiceClient.getAccount(1L)).thenReturn(accountDto);
        when(accountServiceClient.updateBalance(eq(1L), any()))
            .thenReturn(new BalanceUpdateResponse());
    }

    @Test
    @DisplayName("Should complete deposit and withdrawal transaction workflow")
    void shouldCompleteTransactionWorkflow() {
        // Given - Deposit Request
        TransactionRequest depositRequest = TransactionRequest.builder()
            .transactionType("DEPOSIT")
            .destinationAccountId(1L)
            .amount(BigDecimal.valueOf(1000))
            .currency("USD")
            .description("Integration test deposit")
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer test-token");
        HttpEntity<TransactionRequest> depositEntity = new HttpEntity<>(depositRequest, headers);
        
        // When - Create deposit
        ResponseEntity<TransactionDto> depositResponse = restTemplate.postForEntity(
            "/api/transactions", depositEntity, TransactionDto.class);
        
        // Then - Verify deposit
        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(depositResponse.getBody().getAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(depositResponse.getBody().getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        
        // Given - Withdrawal Request
        TransactionRequest withdrawalRequest = TransactionRequest.builder()
            .transactionType("WITHDRAWAL")
            .sourceAccountId(1L)
            .amount(BigDecimal.valueOf(500))
            .currency("USD")
            .description("Integration test withdrawal")
            .build();
            
        HttpEntity<TransactionRequest> withdrawalEntity = new HttpEntity<>(withdrawalRequest, headers);
        
        // When - Create withdrawal
        ResponseEntity<TransactionDto> withdrawalResponse = restTemplate.postForEntity(
            "/api/transactions", withdrawalEntity, TransactionDto.class);
            
        // Then - Verify withdrawal
        assertThat(withdrawalResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(withdrawalResponse.getBody().getAmount()).isEqualTo(BigDecimal.valueOf(500));
        
        // Verify transactions saved in database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(2);
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
mvn test -Dtest=TransactionServiceTest

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Generate test report
mvn surefire-report:report-only
```

## 🔄 Events

### Published Events

#### TransactionCreatedEvent
```java
@Data
@AllArgsConstructor
public class TransactionCreatedEvent {
    private Long transactionId;
    private String transactionReference;
    private String transactionType;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private Long userId;
    private LocalDateTime createdAt;
}
```

#### TransactionCompletedEvent
```java
@Data
@AllArgsConstructor
public class TransactionCompletedEvent {
    private Long transactionId;
    private String transactionReference;
    private String transactionType;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private Long userId;
    private LocalDateTime completedAt;
}
```

#### TransactionFailedEvent
```java
@Data
@AllArgsConstructor
public class TransactionFailedEvent {
    private Long transactionId;
    private String transactionReference;
    private String transactionType;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String failureReason;
    private Long userId;
    private LocalDateTime failedAt;
}
```

#### ExternalTransferStatusChangedEvent
```java
@Data
@AllArgsConstructor
public class ExternalTransferStatusChangedEvent {
    private Long transactionId;
    private String transactionReference;
    private TransactionStatus oldStatus;
    private TransactionStatus newStatus;
    private String statusReason;
    private LocalDateTime updatedAt;
}
```

### Event Publishing
```java
@Service
public class TransactionServiceImpl implements TransactionService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public TransactionDto createTransaction(TransactionRequest request) {
        Transaction transaction = transactionRepository.save(newTransaction);
        
        // Publish event
        eventPublisher.publishEvent(new TransactionCreatedEvent(
            transaction.getId(),
            transaction.getTransactionReference(),
            transaction.getTransactionType().getName(),
            transaction.getSourceAccountId(),
            transaction.getDestinationAccountId(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getStatus(),
            transaction.getUserId(),
            transaction.getCreatedAt()
        ));
        
        return TransactionMapper.toDto(transaction);
    }
}
```

## 🔍 Monitoring & Metrics

### Custom Metrics
```java
@Component
public class TransactionMetrics {
    
    private final Counter transactionCount = Counter.builder("transaction.count")
        .description("Total transaction count")
        .tag("type", "all")
        .register(Metrics.globalRegistry);
        
    private final Counter transactionCompletedCount = Counter.builder("transaction.completed")
        .description("Completed transaction count")
        .tag("type", "all")
        .register(Metrics.globalRegistry);
        
    private final Counter transactionFailedCount = Counter.builder("transaction.failed")
        .description("Failed transaction count")
        .tag("type", "all")
        .register(Metrics.globalRegistry);
        
    private final Timer transactionProcessingTime = Timer.builder("transaction.processing.time")
        .description("Transaction processing time")
        .tag("type", "all")
        .register(Metrics.globalRegistry);
        
    private final DistributionSummary transactionAmount = DistributionSummary.builder("transaction.amount")
        .description("Transaction amount distribution")
        .tag("currency", "USD")
        .scale(100)
        .register(Metrics.globalRegistry);
        
    public void recordTransaction(String type) {
        transactionCount.increment(Tags.of("type", type));
    }
    
    public void recordCompletedTransaction(String type) {
        transactionCompletedCount.increment(Tags.of("type", type));
    }
    
    public void recordFailedTransaction(String type) {
        transactionFailedCount.increment(Tags.of("type", type));
    }
    
    public Timer.Sample startProcessingTimer() {
        return Timer.start(Metrics.globalRegistry);
    }
    
    public void recordTransactionAmount(BigDecimal amount, String currency) {
        transactionAmount.record(amount.doubleValue(), Tags.of("currency", currency));
    }
}
```

### Health Checks
```java
@Component
public class TransactionServiceHealthIndicator implements HealthIndicator {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountServiceClient accountServiceClient;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Health health() {
        try {
            // Check database connectivity
            long transactionCount = transactionRepository.count();
            
            // Check account service connectivity
            boolean accountServiceAvailable = accountServiceClient.healthCheck();
            
            // Check Kafka connectivity
            boolean kafkaAvailable = kafkaTemplate.getMessageConverter() != null;
            
            if (accountServiceAvailable && kafkaAvailable) {
                return Health.up()
                    .withDetail("transactionCount", transactionCount)
                    .withDetail("accountServiceStatus", "UP")
                    .withDetail("kafkaStatus", "UP")
                    .withDetail("status", "All systems operational")
                    .build();
            } else {
                return Health.down()
                    .withDetail("accountServiceStatus", accountServiceAvailable ? "UP" : "DOWN")
                    .withDetail("kafkaStatus", kafkaAvailable ? "UP" : "DOWN")
                    .withDetail("error", "One or more dependencies unavailable")
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

### Transaction Security
- **Transaction Validation**: Multi-level validation (input, balance, limits)
- **Daily Limits**: Configurable per-transaction type limits
- **Fraud Detection**: Pattern recognition and anomaly detection
- **IP/Device Tracking**: Recording source of transaction request
- **Dual Control**: Optional approval for high-value transactions
- **Non-repudiation**: Complete audit trail with timestamps

### API Security
```java
@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("hasRole('USER')")
public class TransactionController {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionRequest request) {
        // Implementation
    }
    
    @GetMapping("/account/{accountId}")
    @PreAuthorize("@securityService.isAccountOwner(#accountId, authentication.name) or hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByAccount(@PathVariable Long accountId) {
        // Implementation
    }
    
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionDto>> searchTransactions() {
        // Implementation for administrators only
    }
}
```

## 🚀 Deployment

### Docker Configuration

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY target/transaction-service-1.0.0.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### Docker Compose
```yaml
services:
  transaction-service:
    build: .
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DATABASE_URL=jdbc:postgresql://postgres:5432/banking_transactions
      - ACCOUNT_SERVICE_URL=http://account-service:8082
      - NOTIFICATION_SERVICE_URL=http://notification-service:8084
      - EUREKA_URL=http://discovery-server:8761/eureka/
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - postgres
      - kafka
      - account-service
      - notification-service
      - discovery-server
    networks:
      - banking-network
```

### Kubernetes Deployment

#### Deployment Manifest
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: transaction-service
  namespace: banking-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: transaction-service
  template:
    metadata:
      labels:
        app: transaction-service
    spec:
      containers:
      - name: transaction-service
        image: banking-system/transaction-service:1.0.0
        ports:
        - containerPort: 8083
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: transactions-url
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-headless.banking-system:9092"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8083
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8083
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
- **Connection Pooling**: HikariCP with optimal settings
- **Query Performance**: Indexes on transaction reference, account IDs, user ID
- **Batch Processing**: Bulk operations for reporting/analytics
- **Partitioning**: Time-based partitioning for transaction history

### Application Optimization
- **Caching**: Caching transaction types and configuration
- **Async Processing**: Non-blocking operations for external transfers
- **Response Pagination**: Efficient retrieval of transaction history
- **Thread Pool Management**: Optimized thread pools for API calls

## 🐛 Troubleshooting

### Common Issues

#### Transaction Failures
```bash
# Check transaction logs
SELECT * FROM transaction_logs WHERE transaction_id = ? ORDER BY created_at DESC;

# Verify account balance
curl http://account-service:8082/api/accounts/1/balance
```

#### Integration Issues
```bash
# Check account service connectivity
curl http://account-service:8082/actuator/health

# Verify Kafka connectivity
docker exec -it kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

#### Performance Issues
```bash
# Check slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 10;

# Monitor transaction processing times
curl http://localhost:8083/actuator/metrics/transaction.processing.time
```

### Log Analysis
```bash
# Application logs
docker logs transaction-service | grep -i "transaction"

# Error logs
docker logs transaction-service | grep ERROR

# Kafka logs
docker logs kafka | grep -i "banking-transactions"
```

## 📚 Additional Resources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Transaction Processing Guidelines](https://www.swift.com/standards/iso-20022)
- [Kafka Streams Documentation](https://kafka.apache.org/documentation/streams/)
- [Financial Industry Standards](https://www.iso20022.org/)

---

**Transaction Service - Banking System v1.0**
