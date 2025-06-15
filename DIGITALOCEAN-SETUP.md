# DigitalOcean Setup Steps

## 1. Sign up for DigitalOcean

If you haven't already, sign up for a DigitalOcean account at https://www.digitalocean.com

## 2. Install Essential Tools

### Windows (PowerShell with Administrator privileges)

```powershell
# Install Chocolatey (Windows package manager)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# Install Docker Desktop
choco install docker-desktop -y

# Install kubectl
choco install kubernetes-cli -y

# Install doctl (DigitalOcean CLI)
choco install doctl -y
```

### MacOS

```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Docker Desktop
brew install --cask docker

# Install kubectl
brew install kubernetes-cli

# Install doctl
brew install doctl
```

### Linux (Ubuntu/Debian)

```bash
# Install Docker
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

# Enable Docker service
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER

# Install kubectl
sudo apt-get update && sudo apt-get install -y apt-transport-https
curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubectl

# Install doctl
cd ~
wget https://github.com/digitalocean/doctl/releases/download/v1.92.0/doctl-1.92.0-linux-amd64.tar.gz
tar xf doctl-1.92.0-linux-amd64.tar.gz
sudo mv doctl /usr/local/bin
```

## 3. Generate a DigitalOcean API Token

1. Log in to your DigitalOcean account
2. Go to API > Generate New Token
3. Name it "Banking System Deployment" and ensure it has both read and write access
4. Copy the token (this is the only time it will be shown)

## 4. Authenticate with DigitalOcean

```bash
doctl auth init
# Paste your API token when prompted
```

## 5. Create a Container Registry

```bash
doctl registry create banking-system --region blr1
doctl registry login
```

## 6. Create a Kubernetes Cluster

```bash
doctl kubernetes cluster create banking-system-cluster --region blr1 --size s-2vcpu-4gb --count 3 --version 1.27
```

## 7. Configure kubectl

```bash
doctl kubernetes cluster kubeconfig save banking-system-cluster
```

## 8. Verify Connection

```bash
kubectl get nodes
```

You should see the nodes of your cluster listed.

## 9. Continue with Deployment

Once you've completed these steps, you can continue with the deployment using the deployment script:

- Windows: `.\deploy-to-digitalocean.ps1`
- MacOS/Linux: `./deploy-to-digitalocean.sh`

## 10. Access the Dashboard (Optional)

You can access the Kubernetes dashboard by running:

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```

Create a service account:

```bash
kubectl create serviceaccount dashboard-admin-sa
kubectl create clusterrolebinding dashboard-admin-sa --clusterrole=cluster-admin --serviceaccount=default:dashboard-admin-sa
```

Get the token:

```bash
kubectl get secret $(kubectl get serviceaccount dashboard-admin-sa -o jsonpath="{.secrets[0].name}") -o jsonpath="{.data.token}" | base64 --decode
```

Start the proxy:

```bash
kubectl proxy
```

Now access the dashboard at: 
http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/

Use the token from the previous step to log in.
