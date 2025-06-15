# Banking System Microservices - Monitoring and Maintenance Guide

This guide provides best practices for monitoring and maintaining your Banking System microservices deployment on DigitalOcean.

## Monitoring

### Built-in Monitoring

All services are configured with Spring Boot Actuator and Prometheus endpoints for monitoring. You can access health and metrics at:

- Health check: `http://<service>:port/actuator/health`
- Metrics: `http://<service>:port/actuator/prometheus`

### Setting up Prometheus and Grafana

1. Deploy Prometheus and Grafana using Helm:

```bash
# Add Prometheus Helm repository
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus and Grafana
helm install monitoring prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace
```

2. Access Grafana:

```bash
# Port-forward the Grafana service
kubectl port-forward svc/monitoring-grafana 3000:80 -n monitoring
```

3. Log in to Grafana at http://localhost:3000 (default credentials: admin/prom-operator)

4. Import the provided dashboards:
   - Navigate to Dashboards > Import
   - Upload the JSON files from the `monitoring/dashboards` directory

### Key Metrics to Monitor

1. **System Health**
   - CPU Usage
   - Memory Usage
   - Disk Space

2. **Microservice Health**
   - Response Times (99th percentile)
   - Error Rates
   - Request Throughput
   - Instance Count

3. **Database Health**
   - Connection Pool Utilization
   - Query Execution Time
   - Transaction Rate
   - Error Rate

4. **Kafka**
   - Consumer Lag
   - Producer Rate
   - Topic Size

### Alerting

Set up alerts in Grafana for critical conditions:

1. High Resource Usage (>85% CPU or memory)
2. High Error Rate (>5% of requests)
3. Slow Response Times (>500ms p99)
4. Service Unavailability

## Maintenance

### Regular Maintenance Tasks

#### Weekly Tasks

1. **Check and Apply Updates**
   ```bash
   # Check for Kubernetes updates
   doctl kubernetes cluster get banking-system-cluster
   
   # Update cluster if needed
   doctl kubernetes cluster upgrade banking-system-cluster --version <new-version>
   ```

2. **Database Maintenance**
   ```bash
   # Connect to the PostgreSQL pod
   kubectl exec -it deployment/postgres -- psql -U postgres -d bankdb
   
   # Run maintenance commands
   VACUUM ANALYZE;
   ```

3. **Log Review**
   ```bash
   # Get logs from all services
   kubectl logs -l app=user-service --tail=1000
   kubectl logs -l app=account-service --tail=1000
   kubectl logs -l app=transaction-service --tail=1000
   kubectl logs -l app=notification-service --tail=1000
   ```

#### Monthly Tasks

1. **Update Application Images**
   - Build and push new Docker images
   - Update Kubernetes deployments
   ```bash
   # Update deployment
   kubectl set image deployment/user-service user-service=registry.digitalocean.com/<registry>/user-service:latest
   ```

2. **Scale Resources as Needed**
   ```bash
   # Scale a deployment
   kubectl scale deployment user-service --replicas=3
   ```

3. **Review and Clean up Resources**
   ```bash
   # List all resources
   kubectl get all
   
   # Delete unused resources
   kubectl delete pod <pod-name>
   ```

4. **Backup Database**
   ```bash
   # Create a backup
   kubectl exec -it deployment/postgres -- pg_dump -U postgres -d bankdb > bankdb_backup_$(date +%Y%m%d).sql
   ```

### Troubleshooting Common Issues

#### Service is Unavailable

1. Check if the pod is running:
   ```bash
   kubectl get pods -l app=<service-name>
   ```

2. Check pod logs:
   ```bash
   kubectl logs <pod-name>
   ```

3. Check if the service is reachable:
   ```bash
   kubectl exec -it <any-pod> -- curl <service-name>:<port>/actuator/health
   ```

#### Database Connection Issues

1. Check if the database pod is running:
   ```bash
   kubectl get pods -l app=postgres
   ```

2. Check database logs:
   ```bash
   kubectl logs <postgres-pod-name>
   ```

3. Test database connection:
   ```bash
   kubectl exec -it <postgres-pod-name> -- psql -U postgres -d bankdb -c "SELECT 1"
   ```

#### Memory/CPU Usage High

1. Identify high-resource pods:
   ```bash
   kubectl top pods
   ```

2. Check detailed metrics in Grafana

3. Scale up as needed:
   ```bash
   kubectl scale deployment <deployment-name> --replicas=<new-count>
   ```

### Disaster Recovery

1. **Database Recovery**
   ```bash
   # Restore from backup
   kubectl exec -it deployment/postgres -- psql -U postgres -d bankdb < bankdb_backup_20250101.sql
   ```

2. **Full Cluster Recovery**
   - Follow the deployment guide to recreate the cluster
   - Restore the database
   - Deploy all services

## Security Maintenance

### Regular Security Tasks

1. **Update Security Patches**
   - Keep base images updated
   - Regularly update dependencies

2. **Review Access Controls**
   ```bash
   # Review Kubernetes RBAC
   kubectl get roles,clusterroles,rolebindings,clusterrolebindings
   ```

3. **Check for Security Vulnerabilities**
   - Use container scanning tools
   - Review dependencies for vulnerabilities

4. **Rotate Credentials**
   - Update secrets in Kubernetes
   ```bash
   kubectl create secret generic banking-secrets \
     --from-literal=SPRING_DATASOURCE_PASSWORD=<new-password> \
     --from-literal=JWT_SECRET=<new-secret> \
     -o yaml --dry-run=client | kubectl apply -f -
   ```

## Scaling Guidelines

### When to Scale Up

1. When CPU usage consistently exceeds 70%
2. When memory usage consistently exceeds 80%
3. When response times increase beyond acceptable thresholds
4. During planned high-traffic events

### How to Scale

1. **Horizontal Pod Autoscaling**
   ```bash
   kubectl autoscale deployment user-service --min=2 --max=5 --cpu-percent=70
   ```

2. **Manual Scaling**
   ```bash
   kubectl scale deployment user-service --replicas=4
   ```

3. **Cluster Scaling**
   ```bash
   doctl kubernetes cluster node-pool update banking-system-cluster pool-1 --count 5
   ```

## Backup Strategy

1. **Database Backups**
   - Daily automated backups
   - Store backups offsite
   - Test restoration regularly

2. **Configuration Backups**
   ```bash
   kubectl get all -A -o yaml > k8s_backup_$(date +%Y%m%d).yaml
   ```

3. **Source Code**
   - Ensure source code is in version control
   - Back up any private repositories

## Updating the Application

1. **Build new Docker images**
   ```bash
   docker build -t registry.digitalocean.com/<registry>/user-service:v1.1 ./user-service
   docker push registry.digitalocean.com/<registry>/user-service:v1.1
   ```

2. **Update Kubernetes deployments**
   ```bash
   kubectl set image deployment/user-service user-service=registry.digitalocean.com/<registry>/user-service:v1.1
   ```

3. **Monitor the rollout**
   ```bash
   kubectl rollout status deployment/user-service
   ```

4. **Rollback if needed**
   ```bash
   kubectl rollout undo deployment/user-service
   ```

By following this guide, you'll ensure your Banking System microservices remain healthy, secure, and performant.
