# User Service Documentation

## Overview
The User Service is responsible for user management, authentication, and profile operations within the Banking System. It serves as the identity provider and manages user lifecycle operations.

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
com.bankingsystem.userservice/
├── controller/           # REST Controllers
│   ├── UserController.java
│   └── AuthController.java
├── service/             # Business Logic
│   ├── UserService.java
│   ├── AuthService.java
│   └── impl/
├── repository/          # Data Access
│   ├── UserRepository.java
│   └── RoleRepository.java
├── model/              # Domain Models
│   ├── User.java
│   ├── Role.java
│   └── Permission.java
├── dto/                # Data Transfer Objects
│   ├── UserDto.java
│   ├── CreateUserRequest.java
│   └── AuthResponse.java
├── config/             # Configuration
│   ├── SecurityConfig.java
│   └── JwtConfig.java
└── exception/          # Custom Exceptions
    ├── UserNotFoundException.java
    └── EmailAlreadyExistsException.java
```

## 🚀 Features

### Core Functionality
- ✅ User Registration and Profile Management
- ✅ Authentication (JWT-based)
- ✅ Authorization (Role-based)
- ✅ Password Management (encryption, reset)
- ✅ Email Verification
- ✅ User Search and Filtering
- ✅ Account Status Management (Active, Suspended, Deleted)

### Security Features
- ✅ JWT Token Generation and Validation
- ✅ Password Encryption (BCrypt)
- ✅ Role-Based Access Control (RBAC)
- ✅ Session Management
- ✅ Rate Limiting
- ✅ Input Validation and Sanitization

## 📊 Database Schema

### Tables

#### users
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);
```

#### roles
```sql
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### user_roles
```sql
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

#### permissions
```sql
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### role_permissions
```sql
CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id),
    permission_id BIGINT REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);
```

### Indexes
```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);
```

## 🔌 API Endpoints

### Authentication Endpoints

#### POST /api/auth/register
Register a new user account.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "address": "123 Main St, City, State 12345"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "emailVerified": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors
- `409 Conflict`: Email already exists

#### POST /api/auth/login
Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "roles": ["USER"]
  }
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials
- `423 Locked`: Account suspended

#### POST /api/auth/refresh
Refresh JWT token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### POST /api/auth/logout
Invalidate user session and tokens.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "message": "Successfully logged out"
}
```

### User Management Endpoints

#### GET /api/users/profile
Get current user's profile information.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "address": "123 Main St, City, State 12345",
  "status": "ACTIVE",
  "emailVerified": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00Z",
  "lastLogin": "2024-01-20T14:15:00Z"
}
```

#### PUT /api/users/profile
Update current user's profile information.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+1234567891",
  "address": "456 Oak Ave, City, State 12345"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567891",
  "address": "456 Oak Ave, City, State 12345",
  "updatedAt": "2024-01-20T15:30:00Z"
}
```

#### POST /api/users/change-password
Change user's password.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewSecurePassword456!",
  "confirmPassword": "NewSecurePassword456!"
}
```

**Response (200 OK):**
```json
{
  "message": "Password successfully changed"
}
```

**Error Responses:**
- `400 Bad Request`: Password validation failed
- `401 Unauthorized`: Current password incorrect

#### GET /api/users/{userId}
Get user information by ID (Admin only).

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "emailVerified": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### GET /api/users
Search and list users with pagination (Admin only).

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `sort`: Sort field (default: createdAt)
- `order`: Sort order (asc/desc, default: desc)
- `search`: Search term (firstName, lastName, email)
- `status`: Filter by status (ACTIVE, SUSPENDED, DELETED)

**Example Request:**
```
GET /api/users?page=0&size=10&search=john&status=ACTIVE&sort=lastName&order=asc
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true
}
```

#### PUT /api/users/{userId}/status
Update user status (Admin only).

**Headers:**
```
Authorization: Bearer <admin_access_token>
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
  "updatedAt": "2024-01-20T16:00:00Z"
}
```

### Email Verification Endpoints

#### POST /api/users/verify-email
Verify email address using verification token.

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "message": "Email successfully verified"
}
```

#### POST /api/users/resend-verification
Resend email verification token.

**Request Body:**
```json
{
  "email": "john.doe@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "Verification email sent"
}
```

### Password Reset Endpoints

#### POST /api/auth/forgot-password
Request password reset token.

**Request Body:**
```json
{
  "email": "john.doe@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "Password reset email sent"
}
```

#### POST /api/auth/reset-password
Reset password using reset token.

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "newPassword": "NewSecurePassword789!",
  "confirmPassword": "NewSecurePassword789!"
}
```

**Response (200 OK):**
```json
{
  "message": "Password successfully reset"
}
```

## 🔧 Configuration

### Application Properties

#### Development Profile (`application-dev.yml`)
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/banking_users
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  security:
    jwt:
      secret: mySecretKey
      expiration: 3600000 # 1 hour
      refresh-expiration: 86400000 # 24 hours

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

logging:
  level:
    com.bankingsystem.userservice: DEBUG
    org.springframework.security: DEBUG

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
  port: 8081

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
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

  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: ${JWT_EXPIRATION:3600000}
      refresh-expiration: ${JWT_REFRESH_EXPIRATION:86400000}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://discovery-server:8761/eureka/}

logging:
  level:
    com.bankingsystem.userservice: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/user-service.log

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
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: true

  security:
    jwt:
      secret: testSecret
      expiration: 3600000
      refresh-expiration: 86400000

eureka:
  client:
    enabled: false

logging:
  level:
    com.bankingsystem.userservice: DEBUG
```

### Environment Variables

#### Required Environment Variables (Production)
```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/banking_users
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secure_password

# JWT Configuration
JWT_SECRET=super_secure_jwt_secret_key_minimum_256_bits
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# Service Discovery
EUREKA_URL=http://discovery-server:8761/eureka/

# Email Configuration (if email verification enabled)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@bankingsystem.com
MAIL_PASSWORD=app_specific_password
```

## 🧪 Testing

### Test Structure
```
src/test/java/com/bankingsystem/userservice/
├── controller/
│   ├── UserControllerTest.java
│   └── AuthControllerTest.java
├── service/
│   ├── UserServiceTest.java
│   └── AuthServiceTest.java
├── repository/
│   ├── UserRepositoryTest.java
│   └── RoleRepositoryTest.java
├── integration/
│   ├── UserServiceIntegrationTest.java
│   └── AuthenticationIntegrationTest.java
└── TestUserServiceApplication.java
```

### Unit Tests

#### Service Layer Test Example
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .password("password123")
            .build();
            
        User savedUser = User.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .passwordHash("encoded_password")
            .status(UserStatus.ACTIVE)
            .build();
            
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserDto result = userService.createUser(request);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .email("existing@example.com")
            .build();
            
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessage("User with email existing@example.com already exists");
    }
}
```

#### Repository Layer Test Example
```java
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        User user = User.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .passwordHash("password_hash")
            .status(UserStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(user);
        
        // When
        Optional<User> found = userRepository.findByEmail("john.doe@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }
    
    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        // Given
        User user = User.builder()
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .passwordHash("hash")
            .status(UserStatus.ACTIVE)
            .build();
        entityManager.persistAndFlush(user);
        
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        
        // Then
        assertThat(exists).isTrue();
    }
}
```

#### Controller Layer Test Example
```java
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .password("password123")
            .build();
            
        UserDto response = UserDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .status(UserStatus.ACTIVE)
            .build();
            
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }
    
    @Test
    @DisplayName("Should return validation error for invalid request")
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .firstName("")  // Invalid: empty first name
            .email("invalid-email")  // Invalid: malformed email
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }
}
```

### Integration Tests

#### Full Service Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("Should complete user registration flow")
    void shouldCompleteUserRegistrationFlow() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .firstName("Integration")
            .lastName("Test")
            .email("integration.test@example.com")
            .password("SecurePassword123!")
            .build();
        
        // When - Register user
        ResponseEntity<UserDto> response = restTemplate.postForEntity(
            "/api/users", request, UserDto.class);
        
        // Then - Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getEmail()).isEqualTo(request.getEmail());
        
        // Verify user saved in database
        Optional<User> savedUser = userRepository.findByEmail(request.getEmail());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFirstName()).isEqualTo("Integration");
    }
}
```

### Test Coverage

#### Running Tests
```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Generate coverage report
mvn jacoco:report
# View report at: target/site/jacoco/index.html
```

#### Coverage Targets
- **Line Coverage**: 85%+
- **Branch Coverage**: 80%+
- **Method Coverage**: 90%+

## 🔄 Events

### Published Events

#### UserCreatedEvent
```java
@Data
@AllArgsConstructor
public class UserCreatedEvent {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
}
```

#### UserUpdatedEvent
```java
@Data
@AllArgsConstructor
public class UserUpdatedEvent {
    private Long userId;
    private String email;
    private Map<String, Object> changedFields;
    private LocalDateTime updatedAt;
}
```

#### UserStatusChangedEvent
```java
@Data
@AllArgsConstructor
public class UserStatusChangedEvent {
    private Long userId;
    private UserStatus oldStatus;
    private UserStatus newStatus;
    private String reason;
    private LocalDateTime changedAt;
}
```

### Event Publishing
```java
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public UserDto createUser(CreateUserRequest request) {
        User user = userRepository.save(newUser);
        
        // Publish event
        eventPublisher.publishEvent(new UserCreatedEvent(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCreatedAt()
        ));
        
        return UserMapper.toDto(user);
    }
}
```

## 🔍 Monitoring & Metrics

### Custom Metrics
```java
@Component
public class UserMetrics {
    
    private final Counter userRegistrations = Counter.builder("user.registrations")
        .description("Total user registrations")
        .register(Metrics.globalRegistry);
        
    private final Timer loginDuration = Timer.builder("user.login.duration")
        .description("User login duration")
        .register(Metrics.globalRegistry);
        
    private final Gauge activeUsers = Gauge.builder("user.active.count")
        .description("Active users count")
        .register(Metrics.globalRegistry, this, UserMetrics::getActiveUserCount);
        
    public void recordRegistration() {
        userRegistrations.increment();
    }
    
    public Timer.Sample startLoginTimer() {
        return Timer.start(Metrics.globalRegistry);
    }
    
    private double getActiveUserCount() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }
}
```

### Health Checks
```java
@Component
public class UserServiceHealthIndicator implements HealthIndicator {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Health health() {
        try {
            long userCount = userRepository.count();
            return Health.up()
                .withDetail("userCount", userCount)
                .withDetail("status", "Service is running")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### Logging Configuration
```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        try {
            // Service logic
            log.info("User created successfully with ID: {}", user.getId());
            return UserMapper.toDto(user);
        } catch (Exception e) {
            log.error("Failed to create user with email: {}", request.getEmail(), e);
            throw e;
        }
    }
}
```

## 🔒 Security Considerations

### Password Security
- **Encryption**: BCrypt with salt rounds
- **Validation**: Minimum 8 characters, mixed case, numbers, symbols
- **History**: Previous 5 passwords cannot be reused
- **Expiration**: Optional password expiration policy

### JWT Security
- **Algorithm**: HMAC SHA-256
- **Expiration**: Short-lived access tokens (1 hour)
- **Refresh Tokens**: Longer-lived (24 hours)
- **Secret Rotation**: Support for key rotation
- **Claims**: Minimal user information only

### API Security
- **Rate Limiting**: Per-user and per-endpoint limits
- **Input Validation**: Comprehensive validation and sanitization
- **CORS**: Configured for specific origins
- **HTTPS**: TLS 1.3 in production
- **Headers**: Security headers (HSTS, CSP, etc.)

## 🚀 Deployment

### Docker Configuration

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY target/user-service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### Docker Compose (Development)
```yaml
services:
  user-service:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DATABASE_URL=jdbc:postgresql://postgres:5432/banking_users
      - EUREKA_URL=http://discovery-server:8761/eureka/
    depends_on:
      - postgres
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
  name: user-service
  namespace: banking-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: banking-system/user-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
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

#### Service Manifest
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: banking-system
spec:
  selector:
    app: user-service
  ports:
  - protocol: TCP
    port: 8081
    targetPort: 8081
  type: ClusterIP
```

## 📈 Performance Optimization

### Database Optimization
- **Connection Pooling**: HikariCP configuration
- **Query Optimization**: Index usage and query analysis
- **Caching**: Redis for frequently accessed user data
- **Read Replicas**: For read-heavy operations

### Application Optimization
- **JVM Tuning**: Memory and GC optimization
- **Async Processing**: Non-blocking operations where possible
- **Caching**: Spring Cache for user profiles
- **Connection Pooling**: Database connection optimization

### Monitoring Performance
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    metrics:
      enabled: true
    prometheus:
      enabled: true
```

## 🐛 Troubleshooting

### Common Issues

#### Authentication Issues
```bash
# Check JWT configuration
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/users/profile

# Verify token expiration
# Check application logs for JWT errors
```

#### Database Connection Issues
```bash
# Check database connectivity
docker exec -it postgres psql -U postgres -d banking_users

# Verify connection pool settings
# Check HikariCP metrics in actuator
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active
```

#### Service Discovery Issues
```bash
# Check Eureka registration
curl http://localhost:8761/eureka/apps

# Verify service health
curl http://localhost:8081/actuator/health
```

### Log Analysis
```bash
# Application logs
docker logs user-service

# Database logs
docker logs postgres

# Filter by log level
docker logs user-service | grep ERROR

# Real-time log monitoring
docker logs -f user-service
```

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://auth0.com/blog/a-look-at-the-latest-draft-for-jwt-bcp/)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)

---

**User Service - Banking System v1.0**
