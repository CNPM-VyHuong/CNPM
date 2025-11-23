# GitHub Actions Deployment - Manual Setup Guide
# Hướng dẫn setup thủ công không cần Azure CLI

## ✅ Chuẩn bị xong: Các files deployment đã tạo!

```
.github/workflows/deploy-azure.yml   ← GitHub Actions workflow
GITHUB_ACTIONS_DEPLOY_GUIDE.md       ← Hướng dẫn chi tiết
infra/main.bicep                     ← Infrastructure template
infra/containerApps.bicep            ← Container Apps template
azure.yaml                           ← AZD config
```

---

## 🎯 Bước 1: Tạo Service Principal trên Azure Portal

### 1.1 Go to Azure Portal
👉 https://portal.azure.com

### 1.2 Tạo Service Principal
1. Search: "App registrations"
2. Click "+ New registration"
3. Name: `fastfood-github-actions`
4. Click "Register"

### 1.3 Get Client ID & Secret
1. Vào app vừa tạo
2. Copy "Application (client) ID" → lưu lại
3. Click "Certificates & secrets"
4. Click "+ New client secret"
5. Copy secret value → lưu lại

### 1.4 Add Permissions
1. Click "API permissions"
2. Click "+ Add a permission"
3. Select "Azure Service Management"
4. Select "Delegated permissions"
5. Check "user_impersonation"
6. Click "Add permissions"
7. Click "Grant admin consent for [your org]"

---

## 📦 Bước 2: Tạo Resource Group & Container Registry (Azure Portal)

### 2.1 Create Resource Group
1. Go to: https://portal.azure.com
2. Click "Resource groups" (left menu)
3. Click "+ Create"
4. Name: `fastfood-rg`
5. Region: `East Asia` (hoặc gần bạn nhất)
6. Click "Review + create" → "Create"

### 2.2 Create Container Registry
1. Search: "Container registries"
2. Click "+ Create"
3. Subscription: (select yours)
4. Resource group: `fastfood-rg`
5. Registry name: `fastfood` + random numbers (e.g., `fastfood12345`)
6. Location: `East Asia`
7. SKU: `Basic`
8. Admin user: Enable ✓
9. Click "Review + create" → "Create"

### 2.3 Get Registry Credentials
1. Vào Container Registry vừa tạo
2. Click "Access keys"
3. Copy:
   - Login server (e.g., `fastfood12345.azurecr.io`)
   - Username (e.g., `fastfood12345`)
   - password

---

## 🔐 Bước 3: Thêm GitHub Secrets

### 3.1 Go to GitHub Secrets
👉 https://github.com/nguyenthuyvyy/CNPM/settings/secrets/actions

### 3.2 Add Secret 1: AZURE_CREDENTIALS

Click "+ New repository secret"

**Name:** `AZURE_CREDENTIALS`

**Value:** (Copy-paste JSON dưới đây, thay giá trị)
```json
{
  "clientId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "clientSecret": "xxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "subscriptionId": "e5b04f1b-736e-4bf9-9397-29e403c7b5bf",
  "tenantId": "a5b8e44e-0d3b-4e2c-85d7-d10bb63c0902"
}
```

**Thay bằng:**
- `clientId` → Application (client) ID từ bước 1.3
- `clientSecret` → Client secret value từ bước 1.3
- `subscriptionId` → e5b04f1b-736e-4bf9-9397-29e403c7b5bf
- `tenantId` → a5b8e44e-0d3b-4e2c-85d7-d10bb63c0902

### 3.3 Add Secret 2: AZURE_REGISTRY_URL

**Name:** `AZURE_REGISTRY_URL`

**Value:** (từ bước 2.3, e.g., `fastfood12345.azurecr.io`)

### 3.4 Add Secret 3: AZURE_REGISTRY_USERNAME

**Name:** `AZURE_REGISTRY_USERNAME`

**Value:** (từ bước 2.3, e.g., `fastfood12345`)

### 3.5 Add Secret 4: AZURE_REGISTRY_PASSWORD

**Name:** `AZURE_REGISTRY_PASSWORD`

**Value:** (password từ bước 2.3)

### 3.6 Add Secret 5: AZURE_SUBSCRIPTION_ID

**Name:** `AZURE_SUBSCRIPTION_ID`

**Value:** `e5b04f1b-736e-4bf9-9397-29e403c7b5bf`

---

## 🚀 Bước 4: Trigger Deployment

### 4.1 Push code to GitHub

```bash
cd d:\cnpm\CNPM-3
git add .
git commit -m "Setup GitHub Actions deployment"
git push origin main
```

### 4.2 Watch Deployment

1. Go to: https://github.com/nguyenthuyvyy/CNPM/actions
2. Click workflow "Azure Deployment"
3. Watch build logs in real-time

---

## ✅ Deployment Checklist

- [ ] Created App Registration on Azure Portal
- [ ] Got Client ID & Client Secret
- [ ] Created Resource Group: `fastfood-rg`
- [ ] Created Container Registry: `fastfood*`
- [ ] Added 5 GitHub Secrets
- [ ] Pushed code to main branch
- [ ] GitHub Actions started deploying

---

## 🔗 After Deployment: Get Service URLs

### 5.1 View in GitHub Actions
After deployment completes, logs will show URLs like:
```
https://fastfood-api-gateway.xxxxx.azurecontainer.io
https://fastfood-eureka-server.xxxxx.azurecontainer.io
https://fastfood-product-service.xxxxx.azurecontainer.io
```

### 5.2 View in Azure Portal
1. Go to: https://portal.azure.com/fastfood-rg
2. Click "Container Apps"
3. Select each app
4. Copy "Application URL" from Ingress section

### 5.3 Share URLs with Users
No installation needed! Users can access directly:
- API Gateway: `https://fastfood-api-gateway.xxxxx.azurecontainer.io`
- Web UI: Access through API Gateway

---

## 🎯 Key Points

✅ **Auto-deployment:** Every push to main = auto-deploy
✅ **Scalable:** Azure handles scaling automatically
✅ **Cheap:** ~$50-80/month with free $200 credits
✅ **Public URLs:** Anyone with link can access
✅ **CI/CD:** No manual deployment needed

---

## 📊 Architecture After Deployment

```
GitHub Push
    ↓
GitHub Actions (build & deploy)
    ↓
Azure Container Registry (push images)
    ↓
Azure Container Apps (deploy services)
    ↓
Public URLs
    ↓
Users access (no installation!)
```

---

## 🆘 Troubleshooting

### Docker build fails in GitHub Actions
- Check Dockerfile exists in each service
- Verify docker build succeeds locally

### Container App won't start
- Check environment variables in bicep template
- View logs: https://portal.azure.com → Container Apps → Logs

### URL not accessible
- Wait 2-3 minutes for deployment to complete
- Check Container App status is "Running"

---

## 📞 Quick Reference

| Item | Link |
|------|------|
| Azure Portal | https://portal.azure.com |
| GitHub Secrets | https://github.com/nguyenthuyvyy/CNPM/settings/secrets/actions |
| GitHub Actions | https://github.com/nguyenthuyvyy/CNPM/actions |
| Container Apps | Search in Azure Portal |

---

## ✨ You're Ready!

1. ✅ Setup Azure Portal resources
2. ✅ Add GitHub Secrets
3. ✅ Push code
4. ✅ Watch deploy!

Good luck! 🚀
