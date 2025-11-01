# ========================================
# Carbon Credit Marketplace - Full Stack Startup Script
# ========================================
# This script starts ALL services using Docker Compose
# ========================================

Write-Host "Carbon Credit Marketplace - Full Stack Startup" -ForegroundColor Green
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
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Starting All Services with Docker Compose..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to docker directory
Set-Location "$PSScriptRoot\infra\docker"

# Check if .env exists
if (-not (Test-Path ".env")) {
    Write-Host "[WARNING] .env file not found!" -ForegroundColor Yellow
    Write-Host "Creating .env from template..." -ForegroundColor Yellow
    
    $envContent = @"
# Database
MYSQL_ROOT_PASSWORD=admin123
MYSQL_DATABASE=carbon_credit_db
MYSQL_PORT=3306

# Backend
ADMIN_BACKEND_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Security
JWT_SECRET=your_jwt_secret_key_at_least_256_bits_long_for_security
JWT_EXPIRATION=86400000

# Frontend CORS
FRONTEND_ORIGIN=http://localhost:3000

# phpMyAdmin
PHPMYADMIN_PORT=8081
"@
    
    $envContent | Out-File -FilePath ".env" -Encoding UTF8
    Write-Host "Successfully created .env file with default values" -ForegroundColor Green
    Write-Host "IMPORTANT: Change JWT_SECRET and MYSQL_ROOT_PASSWORD for production!" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Starting services..." -ForegroundColor Yellow
docker-compose up -d

Write-Host ""
Write-Host "[SUCCESS] Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "[SUCCESS] All Services Started!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Service Status:" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Access Points:" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "   • MySQL:          localhost:3306" -ForegroundColor White
Write-Host "   • phpMyAdmin:     http://localhost:8081" -ForegroundColor White
Write-Host "   • Admin Backend:  http://localhost:8080" -ForegroundColor White
Write-Host "   • Swagger UI:     http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "   • Health Check:   http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Useful Commands:" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "View logs (all):        docker-compose logs -f" -ForegroundColor White
Write-Host "View backend logs:      docker-compose logs -f admin-backend" -ForegroundColor White
Write-Host "Stop all services:      docker-compose down" -ForegroundColor White
Write-Host "Restart backend:        docker-compose restart admin-backend" -ForegroundColor White
Write-Host "Rebuild backend:        docker-compose up -d --build admin-backend" -ForegroundColor White
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Frontend Setup (Separate Terminal):" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "   cd apps\web-portal-next" -ForegroundColor White
Write-Host "   pnpm install" -ForegroundColor White
Write-Host "   pnpm dev" -ForegroundColor White
Write-Host ""
Write-Host "   Then access: http://localhost:3000" -ForegroundColor White
Write-Host ""

Write-Host "Press any key to view backend logs..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host ""
Write-Host "Viewing Admin Backend logs (Ctrl+C to exit)..." -ForegroundColor Yellow
Write-Host ""
docker-compose logs -f admin-backend
