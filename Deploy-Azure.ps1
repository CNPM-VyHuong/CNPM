# 🚀 FastFood CNPM - Azure Deployment Script
# Script này deploy toàn bộ hệ thống lên Azure

param(
    [Parameter(Mandatory=$true)]
    [string]$SubscriptionId,
    
    [Parameter(Mandatory=$true)]
    [string]$ResourceGroupName = "fastfood-rg",
    
    [Parameter(Mandatory=$false)]
    [string]$Location = "eastasia"
)

$ErrorActionPreference = "Stop"

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🚀 FastFood CNPM - Azure Deployment" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login to Azure
Write-Host "📍 Step 1: Connecting to Azure..." -ForegroundColor Yellow
az account set --subscription $SubscriptionId
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to set subscription. Make sure you ran 'az login'" -ForegroundColor Red
    exit 1
}

# Step 2: Create Resource Group
Write-Host "📍 Step 2: Creating Resource Group '$ResourceGroupName' in $Location..." -ForegroundColor Yellow
az group create --name $ResourceGroupName --location $Location | Out-Null
Write-Host "✅ Resource Group created" -ForegroundColor Green

# Step 3: Build Java Services
Write-Host "📍 Step 3: Building Java Services..." -ForegroundColor Yellow
$services = @(
    "api-gateway",
    "eureka_server", 
    "order_service",
    "payment_service",
    "product_service",
    "restaurant-service",
    "user_service",
    "drone_service"
)

foreach ($service in $services) {
    $servicePath = "DoAnCNPM_Backend\$service"
    if (Test-Path $servicePath) {
        Write-Host "   Building $service..." -ForegroundColor Gray
        Push-Location $servicePath
        mvn clean package -DskipTests -q
        Pop-Location
        Write-Host "   ✅ $service built" -ForegroundColor Green
    }
}

# Step 4: Build React Web App
Write-Host "📍 Step 4: Building React Web App..." -ForegroundColor Yellow
if (Test-Path "DoAnCNPM_Frontend\web") {
    Push-Location "DoAnCNPM_Frontend\web"
    npm install
    npm run build
    Pop-Location
    Write-Host "✅ Web app built" -ForegroundColor Green
}

# Step 5: Get Container Registry Credentials
Write-Host "📍 Step 5: Creating Container Registry..." -ForegroundColor Yellow
$registryName = "fastfood$(Get-Random -Minimum 1000 -Maximum 9999)"
az acr create --resource-group $ResourceGroupName --name $registryName --sku Basic --admin-enabled true | Out-Null
$registryUrl = "$(az acr show --resource-group $ResourceGroupName --name $registryName --query loginServer -o tsv)"
$registryUsername = $registryName
$registryPassword = $(az acr credential show --resource-group $ResourceGroupName --name $registryName --query "passwords[0].value" -o tsv)
Write-Host "✅ Container Registry: $registryUrl" -ForegroundColor Green

# Step 6: Login to Container Registry
Write-Host "📍 Step 6: Logging in to Container Registry..." -ForegroundColor Yellow
az acr login --name $registryName

# Step 7: Build and Push Docker Images
Write-Host "📍 Step 7: Building and Pushing Docker Images..." -ForegroundColor Yellow
$dockerImages = @(
    @{ name = "api-gateway"; path = "DoAnCNPM_Backend/api-gateway" },
    @{ name = "eureka-server"; path = "DoAnCNPM_Backend/eureka_server" },
    @{ name = "order-service"; path = "DoAnCNPM_Backend/order_service" },
    @{ name = "payment-service"; path = "DoAnCNPM_Backend/payment_service" },
    @{ name = "product-service"; path = "DoAnCNPM_Backend/product_service" },
    @{ name = "restaurant-service"; path = "DoAnCNPM_Backend/restaurant-service" },
    @{ name = "user-service"; path = "DoAnCNPM_Backend/user_service" },
    @{ name = "drone-service"; path = "DoAnCNPM_Backend/drone_service" },
    @{ name = "web"; path = "DoAnCNPM_Frontend/web" }
)

foreach ($image in $dockerImages) {
    $imageName = $image.name
    $imagePath = $image.path
    
    if (Test-Path $imagePath) {
        Write-Host "   Building and pushing $imageName..." -ForegroundColor Gray
        
        # Build and push
        az acr build --registry $registryName --image "${imageName}:latest" $imagePath --quiet
        Write-Host "   ✅ $imageName pushed" -ForegroundColor Green
    }
}

# Step 8: Deploy Infrastructure with Bicep
Write-Host "📍 Step 8: Deploying Infrastructure (Bicep)..." -ForegroundColor Yellow
az deployment group create `
    --name "fastfood-infra-$(Get-Date -Format 'yyyyMMddHHmmss')" `
    --resource-group $ResourceGroupName `
    --template-file "infra/main.bicep" `
    --parameters location=$Location environmentName="prod" `
    --output table

Write-Host "✅ Infrastructure deployed" -ForegroundColor Green

# Step 9: Deploy Container Apps
Write-Host "📍 Step 9: Deploying Container Apps..." -ForegroundColor Yellow

# Get Infrastructure Outputs
$infraOutputs = az deployment group show --name "fastfood-infra-$(Get-Date -Format 'yyyyMMdd')" `
    --resource-group $ResourceGroupName `
    --query properties.outputs

$containerAppsEnvironmentId = $infraOutputs.containerAppsEnvironmentId.value
$containerRegistryUrl = $infraOutputs.containerRegistryUrl.value

az deployment group create `
    --name "fastfood-apps-$(Get-Date -Format 'yyyyMMddHHmmss')" `
    --resource-group $ResourceGroupName `
    --template-file "infra/containerApps.bicep" `
    --parameters `
        location=$Location `
        containerAppsEnvironmentId=$containerAppsEnvironmentId `
        containerRegistryUrl=$containerRegistryUrl `
        containerRegistryUsername=$registryUsername `
        containerRegistryPassword=$registryPassword `
    --output table

Write-Host "✅ Container Apps deployed" -ForegroundColor Green

# Step 10: Get Deployment Info
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "✅ Deployment Complete!" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

Write-Host "📊 Deployment Summary:" -ForegroundColor Yellow
Write-Host "  Resource Group: $ResourceGroupName" -ForegroundColor White
Write-Host "  Location: $Location" -ForegroundColor White
Write-Host "  Container Registry: $registryUrl" -ForegroundColor White
Write-Host ""

Write-Host "🔗 Service URLs:" -ForegroundColor Yellow
$apps = @("api-gateway", "eureka-server", "product-service", "user-service", "order-service")
foreach ($app in $apps) {
    $url = az containerapp show --name "fastfood-$app" --resource-group $ResourceGroupName --query properties.configuration.ingress.fqdn -o tsv 2>/dev/null
    if ($url) {
        Write-Host "  $app: https://$url" -ForegroundColor Cyan
    }
}

Write-Host ""
Write-Host "💾 Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Update DNS records to point to API Gateway" -ForegroundColor Gray
Write-Host "  2. Configure SSL certificates" -ForegroundColor Gray
Write-Host "  3. Setup monitoring and alerts" -ForegroundColor Gray
Write-Host "  4. Configure auto-scaling policies" -ForegroundColor Gray
Write-Host ""
