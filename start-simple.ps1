# ========================================
# Simple Startup - Database Only
# ========================================
# Starts MySQL and phpMyAdmin only
# Run backend and frontend manually
# ========================================

Write-Host "Carbon Credit Marketplace - Simple Startup" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

# Check Docker
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "[OK] Docker is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker is not running. Start Docker Desktop first!" -ForegroundColor Red
    exit 1
}

# Navigate to docker directory
Set-Location "$PSScriptRoot\infra\docker"

# Create .env if not exists
if (-not (Test-Path ".env")) {
    Write-Host "[INFO] Creating .env file..." -ForegroundColor Yellow
    
    @"
MYSQL_ROOT_PASSWORD=admin123
MYSQL_DATABASE=carbon_credit_db
MYSQL_PORT=3306
ADMIN_BACKEND_PORT=8080
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=your_jwt_secret_key_at_least_256_bits_long_for_security_please_change_this
JWT_EXPIRATION=86400000
FRONTEND_ORIGIN=http://localhost:3000
PHPMYADMIN_PORT=8081
"@ | Out-File -FilePath ".env" -Encoding UTF8
    
    Write-Host "[OK] .env created" -ForegroundColor Green
}

Write-Host ""
Write-Host "Starting MySQL and phpMyAdmin..." -ForegroundColor Yellow
docker-compose up -d mysql phpmyadmin

Write-Host ""
Write-Host "Waiting for MySQL to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Database Services Running!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

docker-compose ps

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Access Points:" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   - MySQL:       localhost:3306" -ForegroundColor White
Write-Host "   - phpMyAdmin:  http://localhost:8081" -ForegroundColor White
Write-Host "   - Username:    root" -ForegroundColor White
Write-Host "   - Password:    admin123" -ForegroundColor White
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Next: Run Backend Manually" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Open a NEW terminal and run:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   cd c:\laptrinhjava\carbon-credit-marketplace\apps\admin-backend-spring" -ForegroundColor White
Write-Host "   mvn spring-boot:run -Dspring-boot.run.arguments=`"--spring.profiles.active=dev`"" -ForegroundColor White
Write-Host ""
Write-Host "Backend will start at: http://localhost:8080" -ForegroundColor Green
Write-Host "Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Next: Run Frontend (Optional)" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Open ANOTHER terminal and run:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   cd c:\laptrinhjava\carbon-credit-marketplace\apps\web-portal-next" -ForegroundColor White
Write-Host "   pnpm install" -ForegroundColor White
Write-Host "   pnpm dev" -ForegroundColor White
Write-Host ""
Write-Host "Frontend will start at: http://localhost:3000" -ForegroundColor Green
Write-Host ""

Write-Host "Press any key to open phpMyAdmin..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
Start-Process "http://localhost:8081"

Write-Host ""
Write-Host "[SUCCESS] Database ready! Follow the instructions above." -ForegroundColor Green
Write-Host ""
