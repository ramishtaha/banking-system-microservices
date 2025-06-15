# Banking System Microservices - DigitalOcean Deployment Guide

This guide will help you deploy the Banking System Microservices on DigitalOcean's Kubernetes platform.

## Prerequisites

Before you start, make sure you have the following:

1. A DigitalOcean account with billing set up
2. The DigitalOcean CLI (`doctl`) installed and configured
3. Docker installed and running on your local machine
4. Kubernetes CLI (`kubectl`) installed on your local machine

## Step-by-Step Deployment

### 1. Install Required Tools

#### DigitalOcean CLI (doctl)

For Windows:
```powershell
# Download the latest release from the GitHub releases page
# https://github.com/digitalocean/doctl/releases

# Extract the ZIP file and add the doctl.exe location to your PATH
```

For macOS:
```bash
brew install doctl
```

For Linux:
```bash
sudo snap install doctl
```

#### Docker

Follow the installation guide at https://docs.docker.com/get-docker/

#### Kubernetes CLI (kubectl)

Follow the installation guide at https://kubernetes.io/docs/tasks/tools/

### 2. Authenticate with DigitalOcean

```bash
doctl auth init
```

Follow the prompts to enter your API token, which you can generate in the DigitalOcean control panel under API > Generate New Token.

### 3. Run the Deployment Script

#### For Windows:

```powershell
.\deploy-to-digitalocean.ps1
```

#### For macOS/Linux:

```bash
chmod +x deploy-to-digitalocean.sh
./deploy-to-digitalocean.sh
```

The script will:

1. Create a Kubernetes cluster on DigitalOcean
2. Set up a container registry
3. Build and push Docker images for all services
4. Deploy the infrastructure components (Postgres, Kafka)
5. Deploy all microservices

### 4. Verify Deployment

Check the status of your deployed pods:

```bash
kubectl get pods
```

Check the status of your services:

```bash
kubectl get services
```

### 5. Access Your Application

To access the API Gateway:

```bash
kubectl port-forward svc/api-gateway 8080:8080
```

Then access the API at: http://localhost:8080

### 6. Clean Up

When you're done with the deployment and want to delete the resources:

```bash
doctl kubernetes cluster delete banking-system-cluster
```

## Architecture

The deployed system includes the following components:

1. **Config Server** - Centralized configuration management
2. **Discovery Server** - Service discovery with Eureka
3. **API Gateway** - Entry point for all client requests
4. **User Service** - Manages user authentication and profiles
5. **Account Service** - Handles banking accounts
6. **Transaction Service** - Processes financial transactions
7. **Notification Service** - Manages customer notifications
8. **PostgreSQL** - Persistent data storage
9. **Kafka** - Message broker for event-driven communication

## Troubleshooting

If you encounter issues during deployment, check the following:

1. **Pod Status:**
   ```bash
   kubectl describe pod <pod-name>
   ```

2. **Logs:**
   ```bash
   kubectl logs <pod-name>
   ```

3. **Events:**
   ```bash
   kubectl get events
   ```

4. **ConfigMaps and Secrets:**
   ```bash
   kubectl get configmaps
   kubectl get secrets
   ```

## Additional Resources

- [DigitalOcean Kubernetes Documentation](https://docs.digitalocean.com/products/kubernetes/)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
