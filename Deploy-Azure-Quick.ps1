# 🚀 Azure Deployment - Quick Start
# This is a simpler version to deploy step by step

Write-Host "================== Azure Quick Deployment ==================" -ForegroundColor Cyan
Write-Host "   FastFood CNPM - Azure Quick Deployment Guide" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Variables
$subscriptionId = "e5b04f1b-736e-4bf9-9397-29e403c7b5bf"
$tenantId = "a5b8e44e-0d3b-4e2c-85d7-d10bb63c0902"
$resourceGroupName = "fastfood-rg"
$location = "eastasia"  # Change to your preferred region
$projectName = "fastfood"

Write-Host "📋 Configuration:" -ForegroundColor Yellow
Write-Host "  ├─ Subscription ID: $subscriptionId" -ForegroundColor Gray
Write-Host "  ├─ Resource Group: $resourceGroupName" -ForegroundColor Gray
Write-Host "  ├─ Location: $location" -ForegroundColor Gray
Write-Host "  └─ Project Name: $projectName" -ForegroundColor Gray
Write-Host ""

# Step 1: Check Prerequisites
Write-Host "📍 STEP 1: Checking Prerequisites..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

$prereqs = @(
    @{ name = "Azure CLI"; cmd = "az --version" },
    @{ name = "Docker"; cmd = "docker --version" },
    @{ name = "Maven"; cmd = "mvn --version" },
    @{ name = "Node.js"; cmd = "node --version" }
)

foreach ($prereq in $prereqs) {
    try {
        $output = & $prereq.cmd 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ $($prereq.name) - OK" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  $($prereq.name) - Not found" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  ❌ $($prereq.name) - Error" -ForegroundColor Red
    }
}

Write-Host ""

# Step 2: Azure Login
Write-Host "📍 STEP 2: Azure Login..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "  Running: az login" -ForegroundColor Gray

$account = az account show 2>/dev/null | ConvertFrom-Json
if ($account) {
    Write-Host "  ✅ Already logged in as: $($account.user.name)" -ForegroundColor Green
} else {
    Write-Host "  ℹ️  Opening browser for login..." -ForegroundColor Cyan
    az login --tenant $tenantId
}

Write-Host ""

# Step 3: Create Resource Group
Write-Host "📍 STEP 3: Creating Resource Group..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

try {
    az group create --name $resourceGroupName --location $location --output none
    Write-Host "  ✅ Resource Group created: $resourceGroupName" -ForegroundColor Green
} catch {
    Write-Host "  ℹ️  Resource Group may already exist" -ForegroundColor Yellow
}

Write-Host ""

# Step 4: Create Container Registry
Write-Host "📍 STEP 4: Creating Container Registry..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

$registryName = "$projectName$(Get-Random -Minimum 1000 -Maximum 9999)"
$registryName = $registryName.Replace("-", "")  # Remove hyphens

Write-Host "  Creating registry: $registryName" -ForegroundColor Gray
az acr create --resource-group $resourceGroupName --name $registryName --sku Basic --admin-enabled true --output none

$registryLoginServer = az acr show --resource-group $resourceGroupName --name $registryName --query loginServer -o tsv
Write-Host "  ✅ Registry created: $registryLoginServer" -ForegroundColor Green

Write-Host ""

# Step 5: Login to Container Registry
Write-Host "📍 STEP 5: Logging in to Container Registry..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

Write-Host "  Running: az acr login --name $registryName" -ForegroundColor Gray
az acr login --name $registryName

Write-Host "  ✅ Logged in to registry" -ForegroundColor Green

Write-Host ""

# Step 6: Build and Push Images
Write-Host "📍 STEP 6: Building and Pushing Docker Images..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

$images = @(
    @{ name = "api-gateway"; path = "DoAnCNPM_Backend/api-gateway" },
    @{ name = "eureka-server"; path = "DoAnCNPM_Backend/eureka_server" },
    @{ name = "order-service"; path = "DoAnCNPM_Backend/order_service" },
    @{ name = "payment-service"; path = "DoAnCNPM_Backend/payment_service" },
    @{ name = "product-service"; path = "DoAnCNPM_Backend/product_service" },
    @{ name = "restaurant-service"; path = "DoAnCNPM_Backend/restaurant-service" },
    @{ name = "user-service"; path = "DoAnCNPM_Backend/user_service" },
    @{ name = "drone-service"; path = "DoAnCNPM_Backend/drone_service" }
)

$imageCount = 0
foreach ($image in $images) {
    $imageName = $image.name
    $imagePath = $image.path
    $imageCount++
    
    Write-Host ""
    Write-Host "  [$imageCount/$($images.Count)] Building: $imageName" -ForegroundColor Yellow
    
    if (Test-Path $imagePath) {
        Write-Host "    Path: $imagePath" -ForegroundColor Gray
        Write-Host "    Building locally..." -ForegroundColor Gray
        
        docker build -t "$registryLoginServer/${imageName}:latest" $imagePath --quiet
        
        Write-Host "    Pushing to registry..." -ForegroundColor Gray
        docker push "$registryLoginServer/${imageName}:latest" --quiet
        
        Write-Host "    ✅ $imageName pushed" -ForegroundColor Green
    } else {
        Write-Host "    ❌ Path not found: $imagePath" -ForegroundColor Red
    }
}

Write-Host ""

# Step 7: Show Results
Write-Host "================== DEPLOYMENT COMPLETE ==================" -ForegroundColor Green
Write-Host "            Deployment Preparation Complete" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""

Write-Host "📊 Summary:" -ForegroundColor Yellow
Write-Host "  ├─ Resource Group: $resourceGroupName" -ForegroundColor Cyan
Write-Host "  ├─ Container Registry: $registryLoginServer" -ForegroundColor Cyan
Write-Host "  ├─ Images Pushed: $($images.Count)" -ForegroundColor Cyan
Write-Host "  └─ Location: $location" -ForegroundColor Cyan
Write-Host ""

Write-Host "🔗 Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Deploy Container Apps with infra/containerApps.bicep" -ForegroundColor Gray
Write-Host "  2. Configure networking and ingress" -ForegroundColor Gray
Write-Host "  3. Setup monitoring and auto-scaling" -ForegroundColor Gray
Write-Host ""

Write-Host "💡 To deploy Container Apps:" -ForegroundColor Cyan
Write-Host "  az deployment group create \" -ForegroundColor White
Write-Host "    --name fastfood-apps \" -ForegroundColor White
Write-Host "    --resource-group $resourceGroupName \" -ForegroundColor White
Write-Host "    --template-file infra/containerApps.bicep \" -ForegroundColor White
Write-Host "    --parameters containerRegistryUrl=$registryLoginServer" -ForegroundColor White
Write-Host ""

Write-Host "Documentation:" -ForegroundColor Yellow
Write-Host "  Read: AZURE_DEPLOYMENT_GUIDE.md" -ForegroundColor Cyan
Write-Host ""
