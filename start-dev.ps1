# ========================================
# Carbon Credit Marketplace - Dev Startup Script
# ========================================
# This script starts all development services
# ========================================

Write-Host "Carbon Credit Marketplace - Development Setup" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "[OK] Docker is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker is not running. Please start Docker Desktop first!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Starting Services..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to docker directory
Set-Location "$PSScriptRoot\infra\docker"

Write-Host "1. Starting MySQL Database..." -ForegroundColor Yellow
docker-compose up -d mysql
Start-Sleep -Seconds 5

Write-Host "2. Starting phpMyAdmin..." -ForegroundColor Yellow
docker-compose up -d phpmyadmin
Start-Sleep -Seconds 3

Write-Host "3. Checking MySQL health..." -ForegroundColor Yellow
$retries = 0
$maxRetries = 10
while ($retries -lt $maxRetries) {
    $health = docker-compose ps mysql --format json | ConvertFrom-Json | Select-Object -ExpandProperty Health
    if ($health -eq "healthy") {
        Write-Host "[OK] MySQL is healthy and ready!" -ForegroundColor Green
        break
    }
    $retries++
    Write-Host "[WAIT] Waiting for MySQL to be ready... ($retries/$maxRetries)" -ForegroundColor Yellow
    Start-Sleep -Seconds 5
}

if ($retries -eq $maxRetries) {
    Write-Host "[WARNING] MySQL health check timed out, but continuing..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Services Started!" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Service URLs:" -ForegroundColor Green
Write-Host "   - MySQL:       localhost:3306" -ForegroundColor White
Write-Host "   - phpMyAdmin:  http://localhost:8081" -ForegroundColor White
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend (Admin):" -ForegroundColor Yellow
Write-Host "   cd apps\admin-backend-spring" -ForegroundColor White
Write-Host "   mvn spring-boot:run -Dspring-boot.run.arguments=`"--spring.profiles.active=dev`"" -ForegroundColor White
Write-Host ""
Write-Host "Frontend (Next.js):" -ForegroundColor Yellow
Write-Host "   cd apps\web-portal-next" -ForegroundColor White
Write-Host "   pnpm dev" -ForegroundColor White
Write-Host ""
Write-Host "Or use Docker Compose for backend:" -ForegroundColor Yellow
Write-Host "   docker-compose up -d admin-backend" -ForegroundColor White
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to open phpMyAdmin in browser..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
Start-Process "http://localhost:8081"

Write-Host ""
Write-Host "[SUCCESS] Development environment is ready!" -ForegroundColor Green
Write-Host ""
