# 🚀 Deploy FastFood CNPM lên Azure - Simple Guide

## 📋 Tóm tắt nhanh

Hệ thống sẽ:
- Deploy 8 Java microservices lên Azure
- Deploy 1 React web app
- Tạo public URLs cho bất kỳ ai truy cập
- Auto-deploy khi push code (CI/CD)

---

## ⚡ Quick Start (3 bước)

### Bước 1: Setup Azure Portal
1. Go: https://portal.azure.com
2. Create App Registration (cho GitHub)
3. Create Resource Group: `fastfood-rg`
4. Create Container Registry: `fastfood*`

**→ Chi tiết:** Xem `MANUAL_GITHUB_DEPLOYMENT.md` Bước 1-2

---

### Bước 2: Add GitHub Secrets
1. Go: https://github.com/nguyenthuyvyy/CNPM/settings/secrets/actions
2. Add 5 secrets (client ID, secret, registry URL, etc.)

**→ Chi tiết:** Xem `MANUAL_GITHUB_DEPLOYMENT.md` Bước 3

---

### Bước 3: Push Code
```bash
cd d:\cnpm\CNPM-3
git add .
git commit -m "Deploy to Azure"
git push origin main
```

**GitHub Actions sẽ tự động:**
- Build Docker images
- Push lên Azure
- Deploy services
- Tạo URLs

---

## 🔗 Kết Quả

Sau ~10 phút, bạn sẽ có URLs như:
```
https://fastfood-api-gateway.xxxxx.azurecontainer.io
https://fastfood-eureka-server.xxxxx.azurecontainer.io
https://fastfood-product-service.xxxxx.azurecontainer.io
```

**Chia sẻ links → Users access ngay (không cần cài gì!)**

---

## 📚 Files Để Tham Khảo

- **MANUAL_GITHUB_DEPLOYMENT.md** - Chi tiết 4 bước setup
- **GITHUB_ACTIONS_DEPLOY_GUIDE.md** - Hướng dẫn nâng cao
- **AZURE_DEPLOYMENT_GUIDE.md** - Thông tin Azure resources

---

## ✨ Ready?

Follow `MANUAL_GITHUB_DEPLOYMENT.md` Bước 1-3 → Done! 🎉
