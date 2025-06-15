# Notification Service

## Overview
The Notification Service is a Spring Boot microservice responsible for delivering notifications (Email, SMS, Push, Account Activity, Account Notification) to users within the Banking System. It provides a comprehensive notification delivery system with synchronous REST API operations and asynchronous processing via Kafka. The service ensures reliable message delivery, maintains notification history, and offers robust error handling and retry mechanisms.

## Features
- Multiple notification channels:
  - Email notifications via SMTP
  - SMS notifications via external SMS gateway
  - Push notifications for mobile devices
  - Account activity notifications
  - General account notifications
- Asynchronous processing with Kafka event consumers
- Complete RESTful API for notification management
- Comprehensive notification history storage
- Pagination support for all list endpoints
- Robust error handling with retry capabilities
- Input validation with descriptive error messages
- High test coverage (unit and integration tests)

## Architecture

### Component Layers
- **Controller Layer:** Exposes REST endpoints for sending and retrieving notifications
- **Service Layer:** Implements business logic for different notification channels and error handling
- **Repository Layer:** Manages notification persistence using Spring Data JPA
- **Kafka Consumer:** Processes events from other services and triggers appropriate notifications
- **DTO Layer:** Provides data transfer objects for API requests and responses

### System Interactions
```
┌─────────────────┐     ┌──────────────────┐     ┌───────────────────┐
│  User Service   │────▶│   Kafka Topics   │────▶│                   │
└─────────────────┘     │   - user-events  │     │                   │
                        └──────────────────┘     │                   │
┌─────────────────┐     ┌──────────────────┐     │ Notification      │
│ Account Service │────▶│   Kafka Topics   │────▶│ Service           │────▶ Email SMTP Server
└─────────────────┘     │ - account-events │     │                   │
                        └──────────────────┘     │                   │────▶ SMS Gateway
┌─────────────────┐     ┌──────────────────┐     │                   │
│ Transaction Svc │────▶│   Kafka Topics   │────▶│                   │────▶ Push Notification
└─────────────────┘     │- transaction-evts│     └───────────────────┘      Service
                        └──────────────────┘
```

### Database Schema
```
┌─────────────────────────────────┐
│            notifications        │
├─────────────────────────────────┤
│ id: BIGINT (PK)                 │
│ user_id: BIGINT (NOT NULL)      │
│ subject: VARCHAR(255) (NOT NULL)│
│ content: TEXT (NOT NULL)        │
│ type: VARCHAR(50) (NOT NULL)    │
│ recipient: VARCHAR(255) (NOT NULL)│
│ sent: BOOLEAN (NOT NULL)        │
│ sent_at: TIMESTAMP              │
│ created_at: TIMESTAMP (NOT NULL)│
│ error_message: VARCHAR(255)     │
└─────────────────────────────────┘
```

## API Endpoints

### Create Notification
`POST /api/notifications`
**Request Body:**
```json
{
  "userId": 1,
  "subject": "Welcome to Banking System",
  "content": "Thank you for creating your account.",
  "type": "EMAIL",  // EMAIL, SMS, PUSH, ACCOUNT_ACTIVITY, ACCOUNT_NOTIFICATION
  "recipient": "user@example.com"
}
```
**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 1,
  "subject": "Welcome to Banking System",
  "content": "Thank you for creating your account.",
  "type": "EMAIL",
  "recipient": "user@example.com",
  "sent": true,
  "sentAt": "2024-06-15T12:00:00",
  "createdAt": "2024-06-15T11:59:00",
  "errorMessage": null,
  "message": "Thank you for creating your account."
}
```

### Get All Notifications (Paginated)
`GET /api/notifications?page=0&size=10&sort=createdAt,desc`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "subject": "Welcome to Banking System",
      "content": "Thank you for creating your account.",
      "type": "EMAIL",
      "recipient": "user@example.com",
      "sent": true,
      "sentAt": "2024-06-15T12:00:00",
      "createdAt": "2024-06-15T11:59:00",
      "errorMessage": null,
      "message": "Thank you for creating your account."
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "orders": [
        {
          "direction": "DESC",
          "property": "createdAt"
        }
      ]
    }
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### Get Notifications by User ID (Paginated)
`GET /api/notifications/user/{userId}?page=0&size=10&sort=createdAt,desc`

**Response:** `200 OK`
```json
{
  "content": [/* notification objects */],
  "pageable": { /* pagination metadata */ },
  "totalElements": 5,
  "totalPages": 1
}
```

### Get Notifications by Type (Paginated)
`GET /api/notifications/type/{type}?page=0&size=10&sort=createdAt,desc`

Where `{type}` is one of: `EMAIL`, `SMS`, `PUSH`, `ACCOUNT_ACTIVITY`, `ACCOUNT_NOTIFICATION`

**Response:** `200 OK`
```json
{
  "content": [/* notification objects */],
  "pageable": { /* pagination metadata */ },
  "totalElements": 3,
  "totalPages": 1
}
```

### Get Notifications by User ID and Type (Paginated)
`GET /api/notifications/user/{userId}/type/{type}?page=0&size=10&sort=createdAt,desc`

**Response:** `200 OK`
```json
{
  "content": [/* notification objects */],
  "pageable": { /* pagination metadata */ },
  "totalElements": 2,
  "totalPages": 1
}
```

### Resend Pending Notifications
`POST /api/notifications/resend-pending`

**Response:** `200 OK`

## Kafka Event Handling

The notification service listens to the following Kafka topics:

### Topics
- `user-events`: User registration, profile updates, password changes
- `account-events`: Account creation, status changes, balance thresholds
- `transaction-events`: Deposits, withdrawals, transfers, payment processing

### Event Processing
1. The `NotificationEventConsumer` processes events from all topics
2. Events are mapped to appropriate notification types
3. Notifications are created and delivered via the appropriate channel (Email, SMS, Push)
4. Delivery status and history are persisted in the database

## Testing Strategy

### Unit Tests
- **Service Tests:**
  - `EmailServiceTest`: Tests email delivery, template rendering, and error handling
  - `SmsServiceTest`: Tests SMS gateway integration and message formatting
  - `PushNotificationServiceTest`: Tests push notification delivery
  - `NotificationServiceTest`: Tests business logic, validation, and service coordination

- **Controller Tests:**
  - `NotificationControllerTest`: Tests API endpoints, request validation, and response mapping

- **Repository Tests:**
  - `NotificationRepositoryTest`: Tests database operations and query methods

- **Kafka Tests:**
  - `NotificationEventConsumerTest`: Tests event consumption and processing logic

- **DTO Tests:**
  - `NotificationRequestTest`: Tests request validation and constraints

### Integration Tests
- Uses H2 in-memory database with test profile
- Tests full request/response cycles and persistence
- Validates Kafka consumer integration with mocked producers

### Testing Best Practices
- External dependencies (SMTP, SMS gateway) are mocked
- Comprehensive edge cases and error scenarios
- High code coverage targets (>80%)
- Test data fixtures for consistency

## Configuration

### Spring Profiles
- `dev`: Local development with embedded H2 database
- `test`: Testing configuration with H2 and mocked external services
- `prod`: Production configuration for deployment

### Key Properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/notification_db
spring.datasource.username=postgres
spring.datasource.password=password

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=banking.app@example.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# SMS Configuration
sms.api.key=your-sms-gateway-api-key
sms.api.url=https://api.sms-gateway.com/send

# Push Notification
push.firebase.server-key=your-firebase-server-key
```

## Error Handling & Validation

### Input Validation
- Bean Validation (Jakarta Validation) annotations on DTOs:
  - `@NotNull`: Ensures required fields are present
  - `@NotBlank`: Ensures string fields are not empty
  - `@Size`: Validates string length constraints
  - Custom validators for complex business rules

### Error Handling
- Global exception handler for consistent error responses
- Descriptive error messages with appropriate HTTP status codes
- Notification delivery failures are logged and retried
- Failed notifications store error details for troubleshooting

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL (or use embedded H2 with dev profile)
- Kafka & Zookeeper (or use test profile to disable Kafka)

### Setup Steps
1. Clone the repository
2. Configure `application-dev.properties` with your local settings
3. Start Kafka and Zookeeper (if using Kafka)
4. Run: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

### Testing the Service
1. Run all tests: `mvn test`
2. View test coverage reports: `target/site/jacoco/index.html`
3. Test APIs with Swagger UI: `http://localhost:8083/swagger-ui.html`

## Production Deployment

### Deployment Options
- Docker containers with Docker Compose
- Kubernetes cluster
- Cloud services (AWS ECS, Google Cloud Run, Azure Container Apps)

### Infrastructure Requirements
- PostgreSQL database
- Kafka cluster
- SMTP server access
- SMS gateway account
- Push notification service credentials

### Monitoring & Observability
- Actuator endpoints for health checks and metrics
- Prometheus metric collection
- Logging to centralized log management system
- Tracing with Zipkin/Jaeger for request tracking

## Security Considerations
- API authentication with JWT tokens
- TLS for all service communications
- Encrypted credentials in secure vault
- Input validation against injection attacks
- Rate limiting for API endpoints

## Contributing
- Write unit and integration tests for all new features
- Follow code style guidelines and maintain Javadoc documentation
- Submit pull requests with comprehensive descriptions
- Ensure backward compatibility or document breaking changes

## License
MIT
