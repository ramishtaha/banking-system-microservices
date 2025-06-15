# Microservices Banking System

A production-ready, enterprise-grade banking system built with microservices architecture using Java 17, Spring Boot 3.x, Apache Kafka, PostgreSQL, and Kubernetes. This system demonstrates best practices in distributed systems, event-driven architecture, and cloud-native development.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [System Components](#system-components)
- [Technology Stack](#technology-stack)
- [Key Features](#key-features)
- [Getting Started](#getting-started)
- [Development Guide](#development-guide)
- [Testing Strategy](#testing-strategy)
- [Deployment](#deployment)
- [Monitoring & Observability](#monitoring--observability)
- [Security](#security)
- [API Documentation](#api-documentation)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## 🏗️ Architecture Overview

This banking system implements a microservices architecture following Domain-Driven Design (DDD) principles and Event-Driven Architecture (EDA) patterns. The system is designed for high availability, scalability, and fault tolerance.

### System Architecture Diagram
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Mobile App    │    │   Web Client    │    │  Admin Portal   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │      API Gateway        │
                    │  (Load Balancer +       │
                    │   Authentication)       │
                    └────────────┬────────────┘
                                 │
            ┌────────────────────┼────────────────────┐
            │                    │                    │
   ┌────────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
   │  User Service   │  │ Account Service │  │Transaction Srv │
   │                 │  │                 │  │                │
   └─────────────────┘  └─────────────────┘  └────────────────┘
            │                    │                    │
            └────────────────────┼────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  Notification Service   │
                    │                         │
                    └─────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │      Apache Kafka       │
                    │   (Event Streaming)     │
                    └─────────────────────────┘
```

### Data Flow Architecture
```
User Request → API Gateway → Service → Database
                    ↓
            Event Publishing → Kafka → Event Consumers
                    ↓
            Notification Service → External APIs (Email/SMS)
```

## 🏢 System Components

### Core Services

#### 1. **User Service** (`user-service/`)
- **Purpose**: User management, authentication, and profile operations
- **Responsibilities**:
  - User registration and authentication
  - Profile management (update, delete)
  - JWT token generation and validation
  - User role and permission management
- **Database**: PostgreSQL (users, roles, permissions tables)
- **Endpoints**: `/api/users/**`
- **Events Published**: `UserCreated`, `UserUpdated`, `UserDeleted`

#### 2. **Account Service** (`account-service/`)
- **Purpose**: Bank account lifecycle management
- **Responsibilities**:
  - Account creation, updates, and closure
  - Account balance management
  - Account type management (Savings, Checking, etc.)
  - Account status tracking
- **Database**: PostgreSQL (accounts, account_types tables)
- **Endpoints**: `/api/accounts/**`
- **Events Published**: `AccountCreated`, `AccountUpdated`, `AccountClosed`
- **Dependencies**: User Service (for user validation)

#### 3. **Transaction Service** (`transaction-service/`)
- **Purpose**: Financial transaction processing and history
- **Responsibilities**:
  - Process transfers, deposits, withdrawals
  - Transaction history and reporting
  - Transaction validation and fraud detection
  - Balance updates coordination
- **Database**: PostgreSQL (transactions, transaction_types tables)
- **Endpoints**: `/api/transactions/**`
- **Events Published**: `TransactionCreated`, `TransactionCompleted`, `TransactionFailed`
- **Dependencies**: Account Service (for balance verification)

#### 4. **Notification Service** (`notification-service/`)
- **Purpose**: Multi-channel notification delivery
- **Responsibilities**:
  - Email, SMS, and Push notification delivery
  - Notification templates and personalization
  - Delivery status tracking and retry logic
  - Event-driven notification triggers
- **Database**: PostgreSQL (notifications, notification_templates tables)
- **Endpoints**: `/api/notifications/**`
- **Events Consumed**: All service events for notification triggers
- **External Integrations**: Email (SMTP), SMS (Twilio), Push (FCM)

### Infrastructure Services

#### 5. **API Gateway** (`api-gateway/`)
- **Purpose**: Single entry point for all client requests
- **Features**:
  - Request routing and load balancing
  - Authentication and authorization
  - Rate limiting and throttling
  - Request/response transformation
  - CORS handling
- **Technology**: Spring Cloud Gateway
- **Port**: 8080

#### 6. **Config Server** (`config-server/`)
- **Purpose**: Centralized configuration management
- **Features**:
  - Environment-specific configurations
  - Dynamic configuration updates
  - Configuration versioning
  - Encrypted sensitive properties
- **Technology**: Spring Cloud Config
- **Port**: 8888

#### 7. **Discovery Server** (`discovery-server/`)
- **Purpose**: Service registry and discovery
- **Features**:
  - Service registration and health checks
  - Load balancing support
  - Failover and circuit breaker patterns
- **Technology**: Netflix Eureka
- **Port**: 8761

## 🛠️ Technology Stack

### Backend Technologies
- **Java 17**: Latest LTS version with modern language features
- **Spring Boot 3.1+**: Application framework with auto-configuration
- **Spring Cloud 2022.x**: Microservices patterns and tools
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data persistence layer
- **Hibernate**: ORM framework
- **Apache Kafka**: Event streaming platform
- **PostgreSQL**: Primary database for all services
- **H2**: In-memory database for testing

### DevOps & Infrastructure
- **Docker**: Containerization
- **Kubernetes**: Container orchestration
- **Helm**: Kubernetes package manager
- **Prometheus**: Metrics collection
- **Grafana**: Monitoring dashboards
- **ELK Stack**: Centralized logging (Elasticsearch, Logstash, Kibana)
- **Jaeger**: Distributed tracing

### Testing & Quality
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **TestContainers**: Integration testing with real databases
- **WireMock**: API mocking for external dependencies
- **SonarQube**: Code quality analysis
- **Jacoco**: Code coverage reporting

## ✨ Key Features

### Business Features
- ✅ **User Management**: Registration, authentication, profile management
- ✅ **Account Operations**: Create, update, close accounts with different types
- ✅ **Transaction Processing**: Transfers, deposits, withdrawals with validation
- ✅ **Multi-Channel Notifications**: Email, SMS, and Push notifications
- ✅ **Real-time Updates**: Event-driven architecture for immediate updates
- ✅ **Transaction History**: Complete audit trail of all operations

### Technical Features
- ✅ **Microservices Architecture**: Independently deployable services
- ✅ **Event-Driven Design**: Asynchronous communication via Kafka
- ✅ **API-First Design**: RESTful APIs with OpenAPI documentation
- ✅ **Cloud-Native**: Kubernetes-ready with 12-factor app principles
- ✅ **High Availability**: Load balancing, failover, and circuit breakers
- ✅ **Security**: JWT authentication, HTTPS, input validation
- ✅ **Observability**: Metrics, logging, and distributed tracing
- ✅ **Data Consistency**: ACID transactions and eventual consistency
- ✅ **Scalability**: Horizontal scaling with stateless services
- ✅ **Fault Tolerance**: Retry mechanisms and graceful degradation

## 🚀 Getting Started

### Prerequisites

#### Required Software
```bash
# Java Development
Java 17 (OpenJDK recommended)
Apache Maven 3.8+

# Containerization
Docker 20.x+
Docker Compose 2.x+

# Kubernetes (choose one)
Minikube 1.25+ (for local development)
Kind 0.17+ (alternative for local)
kubectl 1.25+

# Optional Tools
Git 2.30+
IntelliJ IDEA / VS Code
Postman (for API testing)
```

#### System Requirements
- **RAM**: Minimum 8GB (16GB recommended for full local deployment)
- **CPU**: 4+ cores recommended
- **Disk**: 10GB free space
- **OS**: Windows 10/11, macOS 10.15+, or Linux

### Quick Start (Docker Compose)

1. **Clone the Repository**
```bash
git clone <repository-url>
cd "Banking system"
```

2. **Build All Services**
```bash
mvn clean package -DskipTests
```

3. **Start Infrastructure Services**
```bash
docker-compose up -d postgres kafka zookeeper
```

4. **Start Application Services**
```bash
docker-compose up -d
```

5. **Verify Deployment**
```bash
# Check service health
curl http://localhost:8080/actuator/health

# Access Eureka Dashboard
open http://localhost:8761

# View API Documentation
open http://localhost:8080/swagger-ui.html
```

### Local Development Setup

1. **Start Infrastructure Only**
```bash
docker-compose up -d postgres kafka zookeeper
```

2. **Run Services Individually**
```bash
# Terminal 1 - Config Server
cd config-server && mvn spring-boot:run

# Terminal 2 - Discovery Server
cd discovery-server && mvn spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 4 - User Service
cd user-service && mvn spring-boot:run

# Terminal 5 - Account Service
cd account-service && mvn spring-boot:run

# Terminal 6 - Transaction Service
cd transaction-service && mvn spring-boot:run

# Terminal 7 - Notification Service
cd notification-service && mvn spring-boot:run
```

## 💻 Development Guide

### Project Structure
```
Banking system/
├── api-gateway/              # API Gateway service
├── config-server/            # Configuration server
├── discovery-server/         # Service discovery
├── user-service/             # User management
├── account-service/          # Account operations
├── transaction-service/      # Transaction processing
├── notification-service/     # Notification delivery
├── k8s/                      # Kubernetes manifests
├── docker-compose.yml        # Local development
├── pom.xml                   # Parent POM
└── docs/                     # Additional documentation
```

### Service Port Allocation
```
8080 - API Gateway
8761 - Discovery Server (Eureka)
8888 - Config Server
8081 - User Service
8082 - Account Service
8083 - Transaction Service
8084 - Notification Service
9090 - Prometheus
3000 - Grafana
5432 - PostgreSQL
9092 - Kafka
2181 - Zookeeper
```

### Development Workflow

1. **Create Feature Branch**
```bash
git checkout -b feature/new-feature
```

2. **Follow TDD Approach**
```bash
# Write tests first
mvn test

# Implement feature
# Run tests again
mvn test
```

3. **Code Quality Checks**
```bash
# Run all tests
mvn test

# Check code coverage
mvn jacoco:report

# Code quality analysis
mvn sonar:sonar
```

4. **Build and Test**
```bash
mvn clean package
docker-compose up --build -d
```

### Configuration Management

Each service has multiple configuration profiles:

- **`application.yml`**: Default configuration
- **`application-dev.yml`**: Development environment
- **`application-prod.yml`**: Production environment
- **`application-test.yml`**: Testing environment

Environment-specific properties are managed through the Config Server.

## 🧪 Testing Strategy

### Testing Pyramid

#### Unit Tests (60% of tests)
- **Scope**: Individual classes and methods
- **Tools**: JUnit 5, Mockito
- **Coverage Target**: 80%+
- **Run Command**: `mvn test`

```bash
# Run unit tests for specific service
cd user-service
mvn test

# Generate coverage report
mvn jacoco:report
```

#### Integration Tests (30% of tests)
- **Scope**: Service interactions and database operations
- **Tools**: Spring Boot Test, TestContainers
- **Database**: H2 in-memory for fast execution
- **Run Command**: `mvn verify`

#### End-to-End Tests (10% of tests)
- **Scope**: Complete user workflows
- **Tools**: REST Assured, TestContainers
- **Environment**: Full Docker Compose stack
- **Run Command**: `mvn verify -Pintegration-tests`

### Test Categories

#### Service Layer Tests
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Test implementation
    }
}
```

#### Repository Layer Tests
```java
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldFindByEmail() {
        // Test implementation
    }
}
```

#### Controller Layer Tests
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void shouldCreateUser() throws Exception {
        // Test implementation
    }
}
```

### Running All Tests
```bash
# Run all tests across all services
mvn test

# Run tests with coverage
mvn clean verify jacoco:report

# Run integration tests
mvn verify -Pintegration-tests

# Generate aggregated test report
mvn surefire-report:report-only
```

## 🚢 Deployment

### Docker Deployment

#### Local Environment
```bash
# Build and start all services
docker-compose up --build -d

# Scale specific services
docker-compose up --scale user-service=3 -d

# View logs
docker-compose logs -f user-service

# Stop all services
docker-compose down
```

#### Production Environment
```bash
# Use production configuration
docker-compose -f docker-compose.prod.yml up -d

# Update specific service
docker-compose up --build -d user-service
```

### Kubernetes Deployment

#### Prerequisites
```bash
# Start Minikube (for local)
minikube start --cpus=4 --memory=8g

# Enable ingress
minikube addons enable ingress

# Install Helm
curl https://get.helm.sh/helm-v3.x.x-linux-amd64.tar.gz | tar xz
sudo mv linux-amd64/helm /usr/local/bin/
```

#### Deploy to Kubernetes
```bash
# Create namespace
kubectl create namespace banking-system

# Deploy infrastructure
kubectl apply -f k8s/infrastructure/

# Deploy applications
kubectl apply -f k8s/applications/

# Check deployment status
kubectl get pods -n banking-system

# Access application (Minikube)
minikube service api-gateway -n banking-system
```

#### Helm Deployment
```bash
# Add Helm repository
helm repo add banking-system ./helm-charts

# Install with custom values
helm install banking-system ./helm-charts/banking-system \
  --namespace banking-system \
  --create-namespace \
  --values values-production.yaml

# Upgrade deployment
helm upgrade banking-system ./helm-charts/banking-system

# Rollback if needed
helm rollback banking-system 1
```

### Cloud Deployment (DigitalOcean)

See detailed guides:
- [`DIGITALOCEAN-SETUP.md`](DIGITALOCEAN-SETUP.md)
- [`DEPLOYMENT-GUIDE.md`](DEPLOYMENT-GUIDE.md)
- [`COST-ESTIMATION.md`](COST-ESTIMATION.md)

```bash
# Deploy to DigitalOcean Kubernetes
doctl kubernetes cluster kubeconfig save <cluster-name>
kubectl apply -f k8s/production/
```

## 📊 Monitoring & Observability

### Metrics Collection (Prometheus)
- **URL**: http://localhost:9090
- **Metrics**: Custom application metrics, JVM metrics, HTTP metrics
- **Retention**: 15 days (configurable)

### Monitoring Dashboards (Grafana)
- **URL**: http://localhost:3000
- **Credentials**: admin/admin
- **Pre-configured Dashboards**:
  - Application Overview
  - Service-specific metrics
  - Infrastructure monitoring
  - Business metrics

### Centralized Logging (ELK Stack)
```bash
# Start ELK stack
docker-compose -f docker-compose.elk.yml up -d

# Access Kibana
open http://localhost:5601

# View application logs
# Kibana → Discover → Select index pattern
```

### Distributed Tracing (Jaeger)
```bash
# Start Jaeger
docker run -d --name jaeger \
  -p 16686:16686 \
  -p 14268:14268 \
  jaegertracing/all-in-one:latest

# Access Jaeger UI
open http://localhost:16686
```

### Health Checks

Each service exposes health endpoints:
```bash
# Service health
curl http://localhost:8081/actuator/health

# Detailed health information
curl http://localhost:8081/actuator/health/details

# Service info
curl http://localhost:8081/actuator/info

# Metrics endpoint
curl http://localhost:8081/actuator/prometheus
```

## 🔒 Security

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication
- **Role-Based Access Control**: User, Admin, Super Admin roles
- **OAuth2**: Support for external identity providers
- **Session Management**: Token expiration and refresh

### API Security
- **HTTPS Only**: All production traffic encrypted
- **CORS**: Configured for web clients
- **Rate Limiting**: Protection against abuse
- **Input Validation**: DTO validation with Bean Validation
- **SQL Injection Protection**: Parameterized queries

### Data Protection
- **Encryption at Rest**: Database encryption
- **Encryption in Transit**: TLS 1.3
- **Sensitive Data**: Masked in logs
- **Audit Trail**: Complete transaction history

### Security Headers
```yaml
security:
  headers:
    frame-options: DENY
    content-type-options: nosniff
    xss-protection: 1; mode=block
    hsts: max-age=31536000; includeSubDomains
```

## 📚 API Documentation

### OpenAPI/Swagger Documentation

Each service provides interactive API documentation:

- **API Gateway**: http://localhost:8080/swagger-ui.html
- **User Service**: http://localhost:8081/swagger-ui.html
- **Account Service**: http://localhost:8082/swagger-ui.html
- **Transaction Service**: http://localhost:8083/swagger-ui.html
- **Notification Service**: http://localhost:8084/swagger-ui.html

### API Collections

Postman collections are available in `/docs/postman/`:
- `Banking-System-APIs.postman_collection.json`
- `Banking-System-Environment.postman_environment.json`

### Example API Calls

#### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'
```

#### Create Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "userId": 1,
    "accountType": "SAVINGS",
    "initialBalance": 1000.00
  }'
```

#### Transfer Money
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 500.00,
    "description": "Transfer to savings"
  }'
```

## 🔧 Troubleshooting

### Common Issues

#### Service Discovery Issues
```bash
# Check Eureka dashboard
open http://localhost:8761

# Verify service registration
kubectl get pods -n banking-system
kubectl logs <pod-name> -n banking-system
```

#### Database Connection Issues
```bash
# Check PostgreSQL status
docker-compose ps postgres

# View database logs
docker-compose logs postgres

# Connect to database
docker exec -it postgres psql -U postgres -d banking_db
```

#### Kafka Issues
```bash
# Check Kafka status
docker-compose ps kafka

# List topics
docker exec -it kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# View messages
docker exec -it kafka kafka-console-consumer.sh \
  --topic user-events \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### Debugging Tips

#### Enable Debug Logging
```yaml
logging:
  level:
    com.bankingsystem: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

#### Performance Issues
```bash
# Check service metrics
curl http://localhost:8081/actuator/metrics/jvm.memory.used

# View thread dump
curl http://localhost:8081/actuator/threaddump

# Heap dump (if enabled)
curl http://localhost:8081/actuator/heapdump
```

### Log Analysis

#### Application Logs
```bash
# View logs for specific service
docker-compose logs -f user-service

# Filter by log level
docker-compose logs user-service | grep ERROR

# View last 100 lines
docker-compose logs --tail=100 user-service
```

#### Kubernetes Logs
```bash
# View pod logs
kubectl logs -f pod/<pod-name> -n banking-system

# View logs from all containers in deployment
kubectl logs -f deployment/user-service -n banking-system

# Previous container logs
kubectl logs -p pod/<pod-name> -n banking-system
```

## 🤝 Contributing

### Development Guidelines

1. **Code Standards**
   - Follow Java coding conventions
   - Use meaningful variable and method names
   - Add Javadoc for public APIs
   - Maintain consistent formatting

2. **Git Workflow**
   - Use feature branches
   - Write descriptive commit messages
   - Squash commits before merging
   - Use conventional commit format

3. **Testing Requirements**
   - Write tests for all new features
   - Maintain minimum 80% code coverage
   - Include both positive and negative test cases
   - Test edge cases and error scenarios

4. **Pull Request Process**
   - Create detailed PR descriptions
   - Include test results and coverage reports
   - Request reviews from team members
   - Ensure CI/CD pipeline passes

### Code Review Checklist

- [ ] Code follows project conventions
- [ ] All tests pass
- [ ] Code coverage meets requirements
- [ ] No security vulnerabilities
- [ ] Performance impact assessed
- [ ] Documentation updated
- [ ] Breaking changes documented

### Setting Up Development Environment

```bash
# Fork and clone repository
git clone <your-fork-url>
cd "Banking system"

# Install dependencies
mvn clean install

# Set up pre-commit hooks
cp scripts/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit

# Start development environment
docker-compose up -d postgres kafka
```

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 📞 Support

For support and questions:

- **Documentation**: Check this README and service-specific documentation
- **Issues**: Create GitHub issues for bugs and feature requests
- **Discussions**: Use GitHub Discussions for questions and ideas

## 🔄 Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and release notes.

---

**Built with ❤️ by the Banking System Team**
