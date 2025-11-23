# 🚀 GitHub Actions + Azure Deployment Guide

## 📋 Overview

GitHub Actions sẽ tự động:
1. ✅ Build Docker images (8 services Java + 1 React web)
2. ✅ Push lên Azure Container Registry
3. ✅ Deploy lên Azure Container Apps
4. ✅ Tạo public URLs cho mọi người truy cập

**Mỗi khi bạn push code → tự động deploy!** 🚀

---

## 🔧 Step 1: Setup Azure Resources (First Time Only)

### 1.1 Create Resource Group

```powershell
az login --tenant a5b8e44e-0d3b-4e2c-85d7-d10bb63c0902
az account set --subscription e5b04f1b-736e-4bf9-9397-29e403c7b5bf

az group create --name fastfood-rg --location eastasia
```

### 1.2 Create Container Registry

```powershell
$registryName = "fastfood$(Get-Random -Minimum 10000 -Maximum 99999)"

az acr create --resource-group fastfood-rg `
  --name $registryName `
  --sku Basic `
  --admin-enabled true

# Get credentials
$loginServer = az acr show --resource-group fastfood-rg `
  --name $registryName `
  --query loginServer -o tsv

$username = $registryName
$password = az acr credential show --resource-group fastfood-rg `
  --name $registryName `
  --query "passwords[0].value" -o tsv

Write-Host "Registry URL: $loginServer"
Write-Host "Username: $username"
Write-Host "Password: $password"
```

### 1.3 Create Service Principal for GitHub Actions

```powershell
# Create service principal
$sp = az ad sp create-for-rbac `
  --name "fastfood-github-actions" `
  --role contributor `
  --scopes "/subscriptions/e5b04f1b-736e-4bf9-9397-29e403c7b5bf/resourceGroups/fastfood-rg" `
  --output json | ConvertFrom-Json

# Store credentials JSON
$credentials = @{
    clientId = $sp.appId
    clientSecret = $sp.password
    subscriptionId = "e5b04f1b-736e-4bf9-9397-29e403c7b5bf"
    tenantId = "a5b8e44e-0d3b-4e2c-85d7-d10bb63c0902"
} | ConvertTo-Json

Write-Host "Azure Credentials (copy to GitHub):"
Write-Host $credentials
```

---

## 🔐 Step 2: Add GitHub Secrets

1. Go to GitHub repository
2. Settings → Secrets and variables → Actions
3. Add these secrets:

| Secret Name | Value |
|-------------|-------|
| `AZURE_CREDENTIALS` | Output from `$credentials` above |
| `AZURE_REGISTRY_URL` | e.g., `fastfood12345.azurecr.io` |
| `AZURE_REGISTRY_USERNAME` | `fastfood12345` |
| `AZURE_REGISTRY_PASSWORD` | From `$password` above |
| `AZURE_SUBSCRIPTION_ID` | `e5b04f1b-736e-4bf9-9397-29e403c7b5bf` |

**How to add secrets:**
1. Click "New repository secret"
2. Name: (from table above)
3. Secret: (paste value)
4. Click "Add secret"

---

## 📤 Step 3: Push Code to Trigger Deployment

```bash
git add .
git commit -m "Deploy to Azure"
git push origin main
```

**Watch deployment:**
1. Go to GitHub repo → Actions tab
2. Click workflow "Azure Deployment"
3. Watch build and deploy logs in real-time

---

## 🔍 Step 4: Check Deployment Status

### View logs in GitHub Actions

GitHub automatically shows:
- ✅ Docker build output
- ✅ Push to registry status
- ✅ Azure deployment progress
- ✅ Service URLs after deployment

### View in Azure Portal

```powershell
# Get service URLs
az containerapp show --name fastfood-api-gateway --resource-group fastfood-rg --query properties.configuration.ingress.fqdn

# View logs
az containerapp logs show --name fastfood-api-gateway --resource-group fastfood-rg --follow
```

---

## 🌐 Step 5: Share with Users

After deployment, you'll have URLs like:

```
API Gateway:     https://fastfood-api-gateway.xxxxx.azurecontainer.io
Eureka Server:   https://fastfood-eureka-server.xxxxx.azurecontainer.io
Product Service: https://fastfood-product-service.xxxxx.azurecontainer.io
Web App:         https://fastfood-web.azurewebsites.net
```

**Share these links with anyone** - they can use without installation! 🎉

---

## 🐛 Troubleshooting

### Issue: "Workflow fails with authentication error"

**Solution:** Check GitHub secrets are added correctly
```powershell
# Re-create service principal
az ad sp delete --id <app-id>
az ad sp create-for-rbac --name "fastfood-github-actions" --role contributor
```

### Issue: "Docker build fails"

**Solution:** Check Dockerfile paths in services
```bash
# Verify each service has Dockerfile
find DoAnCNPM_Backend -name "Dockerfile" -type f
```

### Issue: "Container App deployment times out"

**Solution:** Check resource availability
```powershell
az containerapp show --name fastfood-api-gateway --resource-group fastfood-rg --query properties.provisioningState
```

---

## 📊 Monitoring

### View Real-time Logs

```powershell
# Follow container logs
az containerapp logs show --name fastfood-api-gateway --resource-group fastfood-rg --follow

# View all containers
az containerapp list --resource-group fastfood-rg --query "[].name" -o table
```

### Monitor Performance

1. Azure Portal → fastfood-rg → Container Apps
2. Select service → Monitor
3. View CPU, Memory, Requests

---

## 🔄 Auto-deployment on Push

GitHub Actions workflow automatically:

1. Listens for pushes to `main` branch
2. Runs build and deploy jobs
3. Deploys updated services
4. Updates container images in registry

**To update production:** Just push code!

```bash
git push origin main  # Auto-deploys!
```

---

## 🎯 Next Steps

1. ✅ Setup Azure CLI (if needed for manual commands)
2. ✅ Configure GitHub secrets
3. ✅ Push code to trigger deployment
4. ✅ Monitor in GitHub Actions tab
5. ✅ Access services via public URLs
6. ✅ Share with team/users

---

## 📚 Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Azure Container Apps](https://docs.microsoft.com/azure/container-apps/)
- [Azure CLI Reference](https://learn.microsoft.com/cli/azure/)
- [Docker Documentation](https://docs.docker.com/)

---

## 💡 Pro Tips

- **Local testing:** Use `docker-compose.yml` to test locally before pushing
- **Cost optimization:** Use Basic tier for Container Apps (cheapest)
- **Auto-scaling:** Configure in Container Apps settings
- **Monitoring:** Enable Application Insights for observability

---

**Ready to deploy? Push code to GitHub and watch it go live!** 🚀
