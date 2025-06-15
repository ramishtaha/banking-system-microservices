# DigitalOcean Kubernetes Cluster Deployment Script for Windows PowerShell

# You'll need to replace these values with your own
$CLUSTER_NAME = "banking-system-cluster"
$REGION = "blr1"
$NODE_SIZE = "s-2vcpu-4gb"
$NODE_COUNT = 3
$KUBERNETES_VERSION = "1.32.2-do.3"

# Step 1: Authenticate with DigitalOcean
Write-Host "Step 1: Please authenticate with DigitalOcean"
Write-Host "Run the command: doctl auth init"
Write-Host "Follow the prompts to enter your DigitalOcean API token"
Write-Host "----------------------------------------------------"
Write-Host "Press Enter to continue after authenticating..."
Read-Host

# Step 2: Create a Kubernetes cluster
Write-Host "Step 2: Creating a Kubernetes cluster on DigitalOcean"
doctl kubernetes cluster create $CLUSTER_NAME --region $REGION --size $NODE_SIZE --count $NODE_COUNT --version $KUBERNETES_VERSION

# Step 3: Configure kubectl to use the new cluster
Write-Host "Step 3: Configuring kubectl to use the new cluster"
doctl kubernetes cluster kubeconfig save $CLUSTER_NAME

# Step 4: Create a container registry
Write-Host "Step 4: Creating a container registry on DigitalOcean"
doctl registry create banking-system-registry --region $REGION

# Step 5: Log in to the container registry
Write-Host "Step 5: Logging in to the container registry"
doctl registry login

# Step 6: Build and push Docker images
Write-Host "Step 6: Building and pushing Docker images"

# Get registry hostname
$REGISTRY_NAME = $(doctl registry get --format Name --no-header)

# Build and push each service
$services = @("config-server", "discovery-server", "user-service", "account-service", "transaction-service", "notification-service", "api-gateway")

foreach ($service in $services) {
    Write-Host "Building and pushing $service..."
    Set-Location -Path "..\$service"
    
    # Build the Docker image
    docker build -t registry.digitalocean.com/$REGISTRY_NAME/$service`:latest .
    
    # Push the image to DigitalOcean's registry
    docker push registry.digitalocean.com/$REGISTRY_NAME/$service`:latest
    
    # Update Kubernetes deployment to use the image from the registry
    (Get-Content -Path "..\k8s\$service.yaml") -replace "image: banking-system/$service`:latest", "image: registry.digitalocean.com/$REGISTRY_NAME/$service`:latest" | Set-Content -Path "..\k8s\$service.yaml"
    
    Set-Location -Path ".."
}

# Step 7: Deploy supporting infrastructure (PostgreSQL, Kafka)
Write-Host "Step 7: Deploying supporting infrastructure"
kubectl apply -f ./k8s/postgres.yaml
kubectl apply -f ./k8s/kafka.yaml

# Step 8: Deploy the application services
Write-Host "Step 8: Deploying the application services"
kubectl apply -f ./k8s/configmap.yaml
kubectl apply -f ./k8s/secrets.yaml
kubectl apply -f ./k8s/config-server.yaml
kubectl apply -f ./k8s/discovery-server.yaml
kubectl apply -f ./k8s/user-service.yaml
kubectl apply -f ./k8s/account-service.yaml
kubectl apply -f ./k8s/transaction-service.yaml
kubectl apply -f ./k8s/notification-service.yaml
kubectl apply -f ./k8s/api-gateway.yaml

Write-Host "Deployment completed!"
Write-Host "----------------------------------------------------"
Write-Host "To check the status of your pods:"
Write-Host "kubectl get pods"
Write-Host ""
Write-Host "To check the status of your services:"
Write-Host "kubectl get services"
Write-Host ""
Write-Host "Once all pods are running, you can access the API Gateway through:"
Write-Host "kubectl port-forward svc/api-gateway 8080:8080"
Write-Host "Then access the API at: http://localhost:8080"
