# Azure Quick Deployment Script
# Builds and pushes Docker images to Azure Container Registry

param(
    [string]$SubscriptionId = "e5b04f1b-736e-4bf9-9397-29e403c7b5bf",
    [string]$ResourceGroupName = "fastfood-rg",
    [string]$Location = "eastasia"
)

# Configuration
$projectName = "fastfood"
$tenantId = "a5b8e44e-0d3b-4e2c-85d7-d10bb63c0902"

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   FastFood CNPM - Azure Quick Deployment" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# STEP 1: Check Prerequisites
Write-Host "[STEP 1] Checking Prerequisites..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Gray
Write-Host ""

$checks = @()

# Check Azure CLI
try {
    $azVersion = az --version 2>&1 | Select-Object -First 1
    Write-Host "[OK] Azure CLI installed" -ForegroundColor Green
    $checks += "azure-cli"
} catch {
    Write-Host "[FAIL] Azure CLI not found - Install from https://aka.ms/installazurecliwindows" -ForegroundColor Red
}

# Check Docker
try {
    $dockerVersion = docker --version 2>&1
    Write-Host "[OK] Docker installed" -ForegroundColor Green
    $checks += "docker"
} catch {
    Write-Host "[FAIL] Docker not found" -ForegroundColor Red
}

# Check Maven
try {
    $mvnVersion = mvn --version 2>&1 | Select-Object -First 1
    Write-Host "[OK] Maven installed" -ForegroundColor Green
    $checks += "maven"
} catch {
    Write-Host "[FAIL] Maven not found" -ForegroundColor Red
}

Write-Host ""

# STEP 2: Azure Login
Write-Host "[STEP 2] Connecting to Azure..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Gray
Write-Host ""

try {
    $account = az account show 2>&1 | ConvertFrom-Json -ErrorAction SilentlyContinue
    if ($account) {
        Write-Host "[OK] Already logged in as: $($account.user.name)" -ForegroundColor Green
    } else {
        Write-Host "[LOGIN REQUIRED] Opening browser..." -ForegroundColor Yellow
        az login --tenant $tenantId
    }
} catch {
    Write-Host "[ERROR] Login failed: $_" -ForegroundColor Red
}

Write-Host ""

# STEP 3: Set Subscription
Write-Host "[STEP 3] Setting Azure Subscription..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Gray
Write-Host ""

try {
    az account set --subscription $SubscriptionId
    Write-Host "[OK] Subscription set to: $SubscriptionId" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to set subscription: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# STEP 4: Create Resource Group
Write-Host "[STEP 4] Creating Resource Group..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Gray
Write-Host ""

try {
    az group create --name $ResourceGroupName --location $Location --output none
    Write-Host "[OK] Resource Group created: $ResourceGroupName" -ForegroundColor Green
} catch {
    Write-Host "[OK] Resource Group already exists: $ResourceGroupName" -ForegroundColor Yellow
}

Write-Host ""

# STEP 5: Create Container Registry
Write-Host "[STEP 5] Creating Container Registry..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Gray
Write-Host ""

$registryName = "$projectName$(Get-Random -Minimum 10000 -Maximum 99999)"
$registryName = $registryName.Replace("-", "")

Write-Host "Registry Name: $registryName" -ForegroundColor Yellow

try {
    Write-Host "Creating ACR..." -ForegroundColor Gray
    az acr create --resource-group $ResourceGroupName --name $registryName --sku Basic --admin-enabled true --output none
    
    $registryLoginServer = az acr show --resource-group $ResourceGroupName --name $registryName --query loginServer -o tsv
    Write-Host "[OK] Container Registry created: $registryLoginServer" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to create registry: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# STEP 6: Login to ACR
Write-Host "[STEP 6] Logging in to Container Registry..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Gray
Write-Host ""

try {
    az acr login --name $registryName
    Write-Host "[OK] Logged in to registry" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to login to registry: $_" -ForegroundColor Red
}

Write-Host ""

# STEP 7: Build and Push Docker Images
Write-Host "[STEP 7] Building and Pushing Docker Images..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Gray
Write-Host ""

$images = @(
    @{ name = "api-gateway"; path = "DoAnCNPM_Backend\api-gateway" },
    @{ name = "eureka-server"; path = "DoAnCNPM_Backend\eureka_server" },
    @{ name = "order-service"; path = "DoAnCNPM_Backend\order_service" },
    @{ name = "payment-service"; path = "DoAnCNPM_Backend\payment_service" },
    @{ name = "product-service"; path = "DoAnCNPM_Backend\product_service" },
    @{ name = "restaurant-service"; path = "DoAnCNPM_Backend\restaurant-service" },
    @{ name = "user-service"; path = "DoAnCNPM_Backend\user_service" },
    @{ name = "drone-service"; path = "DoAnCNPM_Backend\drone_service" }
)

$successCount = 0
foreach ($img in $images) {
    $imageName = $img.name
    $imagePath = $img.path
    
    Write-Host ""
    Write-Host "Building: $imageName" -ForegroundColor Yellow
    
    if (Test-Path $imagePath) {
        Write-Host "  Path: $imagePath" -ForegroundColor Gray
        
        try {
            Write-Host "  Building Docker image..." -ForegroundColor Gray
            docker build -t "$registryLoginServer/${imageName}:latest" $imagePath --quiet
            
            Write-Host "  Pushing to registry..." -ForegroundColor Gray
            docker push "$registryLoginServer/${imageName}:latest" --quiet
            
            Write-Host "  [OK] $imageName pushed successfully" -ForegroundColor Green
            $successCount++
        } catch {
            Write-Host "  [FAIL] Error: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "  [FAIL] Path not found: $imagePath" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "   DEPLOYMENT PREPARATION COMPLETE" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "  Resource Group: $ResourceGroupName" -ForegroundColor Cyan
Write-Host "  Container Registry: $registryLoginServer" -ForegroundColor Cyan
Write-Host "  Images Built: $successCount / $($images.Count)" -ForegroundColor Cyan
Write-Host "  Location: $Location" -ForegroundColor Cyan
Write-Host ""

Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Deploy Container Apps: az deployment group create --name fastfood-apps --resource-group $ResourceGroupName --template-file infra/containerApps.bicep" -ForegroundColor Gray
Write-Host "  2. Check services status in Azure Portal" -ForegroundColor Gray
Write-Host "  3. Configure monitoring and auto-scaling" -ForegroundColor Gray
Write-Host ""

Write-Host "Documentation: AZURE_DEPLOYMENT_GUIDE.md" -ForegroundColor Cyan
Write-Host ""
