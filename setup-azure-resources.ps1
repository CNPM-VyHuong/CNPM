# Setup Azure Resources for GitHub Actions Deployment
# This script creates all necessary Azure resources

param(
    [string]$SubscriptionId = "e5b04f1b-736e-4bf9-9397-29e403c7b5bf",
    [string]$TenantId = "a5b8e44e-0d3b-4e2c-85d7-d10bb63c0902",
    [string]$ResourceGroupName = "fastfood-rg",
    [string]$Location = "eastasia"
)

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   Azure Resources Setup for GitHub Actions" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login
Write-Host "[STEP 1] Logging in to Azure..." -ForegroundColor Yellow

try {
    $account = az account show 2>&1
    if ($account) {
        Write-Host "[OK] Already logged in" -ForegroundColor Green
    }
} catch {
    Write-Host "[LOGIN] Opening browser..." -ForegroundColor Cyan
    az login --tenant $TenantId
}

Write-Host ""

# Step 2: Set Subscription
Write-Host "[STEP 2] Setting subscription..." -ForegroundColor Yellow

try {
    az account set --subscription $SubscriptionId
    Write-Host "[OK] Subscription set" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to set subscription" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 3: Create Resource Group
Write-Host "[STEP 3] Creating Resource Group..." -ForegroundColor Yellow

try {
    az group create --name $ResourceGroupName --location $Location --output none
    Write-Host "[OK] Resource Group: $ResourceGroupName" -ForegroundColor Green
} catch {
    Write-Host "[OK] Resource Group already exists" -ForegroundColor Yellow
}

Write-Host ""

# Step 4: Create Container Registry
Write-Host "[STEP 4] Creating Container Registry..." -ForegroundColor Yellow

$registryName = "fastfood$(Get-Random -Minimum 10000 -Maximum 99999)"
$registryName = $registryName.Replace("-", "")

Write-Host "Registry Name: $registryName" -ForegroundColor Cyan

try {
    az acr create `
        --resource-group $ResourceGroupName `
        --name $registryName `
        --sku Basic `
        --admin-enabled true `
        --output none
    
    Write-Host "[OK] Container Registry created" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to create registry: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 5: Get Registry Credentials
Write-Host "[STEP 5] Getting Registry Credentials..." -ForegroundColor Yellow

$loginServer = az acr show `
    --resource-group $ResourceGroupName `
    --name $registryName `
    --query loginServer -o tsv

$registryPassword = az acr credential show `
    --resource-group $ResourceGroupName `
    --name $registryName `
    --query "passwords[0].value" -o tsv

$registryUsername = $registryName

Write-Host "[OK] Credentials retrieved" -ForegroundColor Green

Write-Host ""

# Step 6: Create Service Principal
Write-Host "[STEP 6] Creating Service Principal for GitHub Actions..." -ForegroundColor Yellow

$spName = "fastfood-github-actions-$(Get-Random -Minimum 1000 -Maximum 9999)"

try {
    $sp = az ad sp create-for-rbac `
        --name $spName `
        --role contributor `
        --scopes "/subscriptions/$SubscriptionId/resourceGroups/$ResourceGroupName" `
        --output json 2>&1 | ConvertFrom-Json

    Write-Host "[OK] Service Principal created" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to create service principal: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 7: Prepare GitHub Secrets
Write-Host "[STEP 7] Preparing GitHub Secrets..." -ForegroundColor Yellow

$azureCredentials = @{
    clientId = $sp.appId
    clientSecret = $sp.password
    subscriptionId = $SubscriptionId
    tenantId = $TenantId
} | ConvertTo-Json -Compress

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "   SETUP COMPLETE - GitHub Secrets to Add:" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Go to: https://github.com/nguyenthuyvyy/CNPM/settings/secrets/actions" -ForegroundColor Cyan
Write-Host ""

Write-Host "Add these secrets:" -ForegroundColor Yellow
Write-Host ""

Write-Host "1. AZURE_CREDENTIALS:" -ForegroundColor White
Write-Host $azureCredentials -ForegroundColor Cyan
Write-Host ""

Write-Host "2. AZURE_REGISTRY_URL:" -ForegroundColor White
Write-Host $loginServer -ForegroundColor Cyan
Write-Host ""

Write-Host "3. AZURE_REGISTRY_USERNAME:" -ForegroundColor White
Write-Host $registryUsername -ForegroundColor Cyan
Write-Host ""

Write-Host "4. AZURE_REGISTRY_PASSWORD:" -ForegroundColor White
Write-Host $registryPassword -ForegroundColor Cyan
Write-Host ""

Write-Host "5. AZURE_SUBSCRIPTION_ID:" -ForegroundColor White
Write-Host $SubscriptionId -ForegroundColor Cyan
Write-Host ""

Write-Host "========================================================" -ForegroundColor Green
Write-Host "   Next Steps:" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""
Write-Host "1. Copy each secret value above" -ForegroundColor Gray
Write-Host "2. Add to GitHub repository settings" -ForegroundColor Gray
Write-Host "3. Push code to main branch" -ForegroundColor Gray
Write-Host "4. Watch GitHub Actions deploy!" -ForegroundColor Gray
Write-Host ""

Write-Host "Deployment will start automatically on push!" -ForegroundColor Green
Write-Host ""

# Optional: Copy to clipboard
Write-Host "Would you like to save secrets to a file? (y/n)" -ForegroundColor Yellow
$save = Read-Host

if ($save -eq "y") {
    $secretsContent = @"
# GitHub Actions Secrets - FastFood CNPM

## Step 1: Go to GitHub Repository Settings
https://github.com/nguyenthuyvyy/CNPM/settings/secrets/actions

## Step 2: Add these secrets:

### 1. AZURE_CREDENTIALS
$azureCredentials

### 2. AZURE_REGISTRY_URL
$loginServer

### 3. AZURE_REGISTRY_USERNAME
$registryUsername

### 4. AZURE_REGISTRY_PASSWORD
$registryPassword

### 5. AZURE_SUBSCRIPTION_ID
$SubscriptionId

## Step 3: Commit and push to main branch
git add .
git commit -m "Deploy to Azure"
git push origin main

Deployment will start automatically!
"@

    $secretsContent | Out-File -FilePath "azure-secrets.txt" -Encoding UTF8
    Write-Host "[OK] Secrets saved to: azure-secrets.txt" -ForegroundColor Green
}

Write-Host ""
