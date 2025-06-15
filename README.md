# Microservices Banking System

A production-ready, enterprise-grade banking system built with microservices architecture using Java 17, Spring Boot 3.x, Apache Kafka, PostgreSQL, and Kubernetes. This system demonstrates best practices in distributed systems, event-driven architecture, and cloud-native development.

## 💡 My Experience with AI-Assisted Development

This project represents my first journey into AI-assisted development, and I'm excited to share my experience:

### Working with AI on This Project

As a developer building this banking system, I leveraged AI tools to assist with various aspects of the project:

- **Architecture Design**: AI helped conceptualize the microservices architecture and domain boundaries
- **Code Generation**: Scaffolded services and implemented business logic with AI guidance
- **Test Coverage**: Developed robust test suites for all microservices
- **Documentation**: Created comprehensive documentation across all services
- **Troubleshooting**: Used AI to debug issues and improve code quality

### My Personal Experience

"This is my first time building something with AI, and while the response of AI was sometimes slow and could be improved, I am very impressed by the results. The AI assistance helped me create a more comprehensive, well-documented, and robustly tested system than I might have built on my own in the same timeframe. It was particularly helpful with creating extensive documentation and setting up proper test coverage. I look forward to continuing to use AI as a collaborative tool in my development workflow."

*- Ramish Taha, Developer*

### Lessons Learned

- **AI as a Collaborative Tool**: AI works best as a partner in the development process, not a replacement for developer expertise
- **Iterative Improvement**: The project evolved through multiple iterations of AI suggestions and developer refinement
- **Knowledge Expansion**: Working with AI exposed me to best practices I might not have otherwise discovered
- **Balancing AI Input**: Learning when to follow AI suggestions versus when to apply domain-specific knowledge

---

## 📋 Table of Contents

- [Architecture Overview](#️-architecture-overview)
- [System Components](#-system-components)
- [Technology Stack](#-technology-stack)
- [Key Features](#-key-features)
- [Getting Started](#-getting-started)
- [Development Guide](#-development-guide)
- [Testing Strategy](#-testing-strategy)
- [Deployment](#-deployment)
- [Monitoring & Observability](#-monitoring--observability)
- [Security](#-security)
- [API Documentation](#-api-documentation)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

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

## ✨ Key Features

### User Management
- ✅ **Registration & Login**: Email/password, social login options
- ✅ **Profile Management**: Personal details, preferences, settings
- ✅ **Multi-factor Authentication**: SMS/email verification
- ✅ **Role-based Access Control**: Customer, Admin, Manager roles

### Account Management
- ✅ **Multiple Account Types**: Savings, Checking, Fixed Deposit
- ✅ **Balance Tracking**: Real-time balance updates
- ✅ **Account Statements**: Downloadable monthly statements
- ✅ **Interest Calculation**: Automated interest processing

### Transaction Processing
- ✅ **Money Transfers**: Internal and external transfers
- ✅ **Scheduled Payments**: Future-dated and recurring transfers
- ✅ **Transaction History**: Complete audit trail of all operations
- ✅ **Transaction Categories**: Automatic categorization for expenses

### Notifications
- ✅ **Multi-channel Delivery**: Email, SMS, Push notifications
- ✅ **Custom Alerts**: Balance thresholds, large transactions, etc.
- ✅ **Personalized Content**: Dynamic templates with user data
- ✅ **Delivery Tracking**: Read receipts and delivery confirmations

### Security Features
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **API Rate Limiting**: Protection against brute-force attacks
- ✅ **Data Encryption**: All sensitive data encrypted at rest and in transit
- ✅ **Audit Logging**: Comprehensive audit trails for all activities

## 🚀 Getting Started

### Prerequisites

#### Required Software
- Java 17 or higher
- Maven 3.8 or higher
- Docker and Docker Compose
- PostgreSQL (or use provided Docker image)
- Kafka (or use provided Docker image)

### Local Development Setup

1. **Clone the Repository**
```bash
git clone https://github.com/ramishtaha/banking-system-microservices.git
cd banking-system-microservices
```

2. **Build All Services**
```bash
mvn clean install -DskipTests
```

3. **Run with Docker Compose**
```bash
docker-compose up -d
```

### Alternative Startup Options

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

- **default**: Base configuration shared across all environments
- **dev**: Local development configuration
- **test**: Testing configuration with H2 database
- **prod**: Production-ready configuration

## 🧪 Testing Strategy

Our comprehensive testing strategy ensures reliability and stability:

### Testing Pyramid

#### Unit Tests (60% of tests)
- Individual components in isolation
- Mock external dependencies
- Fast execution, high coverage

#### Integration Tests (30% of tests)
- Test component interactions
- Test database operations with H2
- API contract validation

#### End-to-End Tests (10% of tests)
- Full request/response flows
- Simulate real-world scenarios
- API Gateway to database and back

### Testing Tools

- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **TestContainers**: Integration tests with real services
- **RestAssured**: API testing
- **Cucumber**: BDD testing for critical flows
- **JaCoCo**: Code coverage reports

### Test Execution

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Skip tests during build
mvn clean package -DskipTests
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

### Metrics Collection

#### Prometheus & Grafana
```bash
# Deploy monitoring stack
kubectl apply -f k8s/monitoring/

# Access Grafana
kubectl port-forward svc/grafana 3000:3000 -n monitoring
```

Default dashboards include:
- System Overview
- Service Performance
- JVM Metrics
- Transaction Throughput
- Error Rates

### Distributed Tracing

#### Spring Cloud Sleuth & Zipkin
```bash
# Deploy Zipkin
kubectl apply -f k8s/tracing/

# Access Zipkin UI
kubectl port-forward svc/zipkin 9411:9411 -n monitoring
```

### Log Aggregation

#### ELK Stack
```bash
# Deploy ELK stack
kubectl apply -f k8s/logging/

# Access Kibana
kubectl port-forward svc/kibana 5601:5601 -n logging
```

Predefined Log Dashboards:
- Error Analysis
- Service Performance
- User Activity
- Security Events

### Health Checks & Alerts

#### Spring Boot Actuator
All services expose health endpoints:
- `/actuator/health`: Overall service health
- `/actuator/metrics`: Detailed metrics
- `/actuator/prometheus`: Prometheus-format metrics

#### Alerting
Configure alerts for:
- Service availability < 99.9%
- High error rates (> 0.1%)
- Response times > 500ms
- JVM memory usage > 80%
- CPU usage > 70%

## 🔒 Security

### Authentication & Authorization

#### JWT-Based Security
- Stateless authentication via JWT tokens
- Token expiration and refresh mechanism
- Role-based access control (RBAC)

#### API Security
- HTTPS-only communication
- CSRF protection
- XSS prevention
- Content Security Policy

### Encryption

All sensitive data is encrypted:
- Data at rest: Database-level encryption
- Data in transit: TLS 1.3
- Personal data: Field-level encryption

### Secure Configuration

- Secret management with Kubernetes Secrets
- Encrypted configuration with Spring Cloud Config
- No hardcoded credentials
- Regular security audits

## 📝 API Documentation

### Swagger Documentation

Each service provides interactive API documentation using OpenAPI 3.0 (Swagger):

- API Gateway: `http://localhost:8080/swagger-ui.html`
- User Service: `http://localhost:8081/swagger-ui.html`
- Account Service: `http://localhost:8082/swagger-ui.html`
- Transaction Service: `http://localhost:8083/swagger-ui.html`
- Notification Service: `http://localhost:8084/swagger-ui.html`

### API Conventions

- All APIs use JSON for request/response
- Authentication via JWT Bearer token
- Consistent error formats
- Paginated responses for collection endpoints
- Hypermedia links (HATEOAS) for resource navigation

### Sample API Calls

#### User Registration
```bash
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123",
    "phoneNumber": "+1234567890"
  }'
```

#### Account Creation
```bash
curl -X POST "http://localhost:8080/api/accounts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "userId": 1,
    "type": "SAVINGS",
    "initialDeposit": 1000.00,
    "currency": "USD"
  }'
```

#### Create Transaction
```bash
curl -X POST "http://localhost:8080/api/transactions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 500.00,
    "description": "Transfer to savings"
  }'
```

## 🔧 Troubleshooting

### Common Issues

#### Connection Errors
```bash
# Check service availability
curl http://localhost:8761/eureka/apps

# Restart discovery service
docker-compose restart discovery-server

# Check connection between services
docker network inspect banking-system_default
```

#### Database Issues
```bash
# Check database availability
docker-compose exec postgres pg_isready

# View database logs
docker-compose logs postgres

# Connect to database directly
docker-compose exec postgres psql -U postgres
```

#### Kafka Problems
```bash
# Check topic list
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# View consumer groups
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Create missing topic
docker-compose exec kafka kafka-topics --create --topic missing-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### Diagnostic Tools

#### JVM Profiling
```bash
# Attach jstack to running process
jstack <pid> > thread-dump.txt

# Memory analysis
jmap -dump:format=b,file=heap-dump.bin <pid>
```

#### Actuator Endpoints
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

**Built with ❤️ by Ramish Taha, with AI assistance**
