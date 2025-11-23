# 🚀 Azure Deployment Guide - FastFood CNPM

## 📋 Prerequisites

- ✅ Azure Account (subscription ID: `e5b04f1b-736e-4bf9-9397-29e403c7b5bf`)
- ✅ Azure CLI installed
- ✅ Docker Desktop running
- ✅ Git installed
- ✅ Maven (for Java build)
- ✅ Node.js (for React build)

## 🔧 Installation

### 1. Install Azure CLI

**Option A: Using Chocolatey (Windows)**
```powershell
choco install azure-cli -y
```

**Option B: Download Installer**
👉 https://aka.ms/installazurecliwindows

**Option C: Using Windows Package Manager**
```powershell
winget install -e --id Microsoft.AzureCLI
```

### 2. Login to Azure

```powershell
# Open browser and login
az login

# Set subscription
az account set --subscription "e5b04f1b-736e-4bf9-9397-29e403c7b5bf"

# Verify
az account show
```

### 3. Verify Prerequisites

```powershell
# Check Azure CLI
az --version

# Check Docker
docker --version

# Check Maven
mvn --version

# Check Node.js
node --version
npm --version
```

---

## 🚀 Quick Deployment (5 minutes)

### Step 1: Navigate to Project

```powershell
cd d:\cnpm\CNPM-3
```

### Step 2: Run Deployment Script

```powershell
# Full deployment
.\Deploy-Azure.ps1 -SubscriptionId "e5b04f1b-736e-4bf9-9397-29e403c7b5bf" -ResourceGroupName "fastfood-rg" -Location "eastasia"
```

**Parameters:**
- `-SubscriptionId`: Azure subscription ID
- `-ResourceGroupName`: Name for Azure resource group (default: "fastfood-rg")
- `-Location`: Azure region (options: eastasia, southeastasia, westus, eastus, etc.)

### Step 3: Wait for Deployment

⏱️ **Estimated time: 10-15 minutes**

The script will:
1. ✅ Create Resource Group
2. ✅ Build Java services (Maven)
3. ✅ Build React web app (npm)
4. ✅ Create Container Registry
5. ✅ Build Docker images (8 Java services + 1 React web)
6. ✅ Push images to Azure Container Registry
7. ✅ Deploy infrastructure (PostgreSQL, MongoDB, etc.)
8. ✅ Deploy Container Apps (all 8 microservices)
9. ✅ Display service URLs

### Step 4: Access Your Application

After deployment completes, you'll see URLs like:
```
🔗 Service URLs:
  api-gateway: https://fastfood-api-gateway.xxxxx.azurecontainer.io
  eureka-server: https://fastfood-eureka-server.xxxxx.azurecontainer.io
  product-service: https://fastfood-product-service.xxxxx.azurecontainer.io
  ...
```

**Share these links with anyone** - they can access without installing anything! 🎉

---

## 📊 What Gets Deployed

### Azure Resources Created

| Resource | Purpose | Cost |
|----------|---------|------|
| **Container Apps** | Host 8 Java microservices | $0.03/hour |
| **Container Registry** | Store Docker images | $5/month |
| **PostgreSQL** | Database for Order/Product/User | $13/month |
| **Cosmos DB** | MongoDB for Payment/Restaurant/Drone | $24/month |
| **Log Analytics** | Monitoring and logs | $0.70/GB |
| **App Service** | Host React web app | $5/month |

**Total: ~$50-80/month** (eligible for $200 free Azure credits)

### Services Deployed

#### Backend Microservices
1. **API Gateway** (8085) - Entry point
2. **Eureka Server** (8761) - Service registry
3. **Order Service** (8082) - Order management
4. **Payment Service** (8083) - Payment processing
5. **Product Service** (8088) - Product catalog
6. **Restaurant Service** (8084) - Restaurant data
7. **User Service** (8081) - User management
8. **Drone Service** (8086) - Drone management

#### Frontend
- **React Web App** - Access at `https://fastfood-web.azurewebsites.net`

---

## 🔍 Monitoring & Management

### View Logs

```powershell
# View Container App logs
az containerapp logs show --name fastfood-api-gateway --resource-group fastfood-rg --follow

# View Application Insights
# Visit: https://portal.azure.com → fastfood-rg → Application Insights
```

### Monitor Performance

1. **Azure Portal**
   ```
   https://portal.azure.com → fastfood-rg
   ```

2. **Application Insights**
   ```
   Monitor live traffic, errors, and performance
   ```

3. **Container Logs**
   ```
   az containerapp logs show --follow
   ```

---

## 🌐 Setup Custom Domain (Optional)

### Add Custom Domain

```powershell
# Get API Gateway FQDN
az containerapp show --name fastfood-api-gateway --resource-group fastfood-rg --query properties.configuration.ingress.fqdn

# Add SSL certificate
az containerapp ingress update --name fastfood-api-gateway --resource-group fastfood-rg --certificate-file cert.pfx
```

### Map Domain Name

1. Go to your domain provider (GoDaddy, Namecheap, etc.)
2. Create CNAME record:
   ```
   api.fastfood.com  →  fastfood-api-gateway.xxxxx.azurecontainer.io
   ```

---

## 🛠️ Manual Deployment Steps (if script fails)

### Build Docker Images

```powershell
# Navigate to each service
cd DoAnCNPM_Backend/api-gateway
docker build -t fastfood/api-gateway:latest .

# Push to registry
az acr build --registry <registry-name> --image api-gateway:latest .
```

### Deploy Specific Service

```powershell
# Deploy single container app
az containerapp create \
  --name fastfood-api-gateway \
  --resource-group fastfood-rg \
  --environment fastfood-env \
  --image <registry>.azurecr.io/api-gateway:latest \
  --ingress external \
  --target-port 8085
```

---

## 🔐 Security Best Practices

### 1. Store Credentials

```powershell
# Don't expose credentials in scripts
# Use Azure Key Vault instead
az keyvault create --resource-group fastfood-rg --name fastfood-kv

# Store secrets
az keyvault secret set --vault-name fastfood-kv --name db-password --value "your-password"
```

### 2. Enable Azure Firewall

```powershell
# Restrict access to Container Apps
az containerapp update \
  --name fastfood-api-gateway \
  --resource-group fastfood-rg \
  --allow-insecure false
```

### 3. Configure SSL/TLS

- All Container Apps use HTTPS by default
- Custom domains require SSL certificates
- Use Azure Certificate Manager for automatic renewal

---

## 💾 Backup & Disaster Recovery

### Backup Database

```powershell
# PostgreSQL backup
az postgres server backup create \
  --name fastfood-postgres \
  --resource-group fastfood-rg \
  --backup-name daily-backup
```

### Container Image Registry

- Images are stored in Azure Container Registry
- Automatic cleanup of old images after 30 days
- Backup to another region for disaster recovery

---

## 🐛 Troubleshooting

### Issue: "Azure CLI not recognized"
**Solution**: Install Azure CLI from https://aka.ms/installazurecliwindows

### Issue: "Subscription not found"
**Solution**: Verify subscription ID
```powershell
az account list --output table
```

### Issue: "Authentication failed"
**Solution**: Login again
```powershell
az logout
az login
```

### Issue: "Container push failed"
**Solution**: Check Docker is running and ACR credentials
```powershell
docker ps
az acr login --name <registry-name>
```

### Issue: "Service not accessible after deployment"
**Solution**: Check Container App status
```powershell
az containerapp show --name fastfood-api-gateway --resource-group fastfood-rg --query properties.provisioningState
```

---

## 📞 Support

- **Azure Documentation**: https://docs.microsoft.com/azure/
- **Container Apps**: https://docs.microsoft.com/azure/container-apps/
- **Azure CLI**: https://learn.microsoft.com/cli/azure/

---

## 🎯 Next Steps After Deployment

1. ✅ **Test Services**
   ```powershell
   # Test API Gateway
   curl https://fastfood-api-gateway.xxxxx.azurecontainer.io/actuator/health
   ```

2. ✅ **Configure Monitoring**
   - Set up Application Insights alerts
   - Create dashboards in Azure Portal

3. ✅ **Setup CI/CD Pipeline**
   - Connect GitHub repo
   - Enable automatic deployment on push

4. ✅ **Add Custom Domain**
   - Map your domain name
   - Configure SSL certificate

5. ✅ **Scale Services**
   - Adjust CPU/Memory as needed
   - Configure auto-scaling policies

---

## 📝 Environment Variables

Services use these environment variables (auto-configured):

```env
# Service Discovery
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://fastfood-postgres:5432/fastfood
SPRING_DATA_MONGODB_URI=mongodb://fastfood-mongo:27017/fastfood

# Monitoring
APPLICATIONINSIGHTS_CONNECTION_STRING=<auto-configured>
```

---

## 🚀 You're Ready!

Your FastFood CNPM application is now live on Azure! 🎉

**Share your API Gateway URL with anyone** - they can access without installation:
```
https://fastfood-api-gateway.xxxxx.azurecontainer.io
```

Happy coding! 💻
