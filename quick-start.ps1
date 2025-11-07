# =============================================================================
# Quick Start Script (No checks, fast startup)
# =============================================================================
# Usage: .\quick-start.ps1
# =============================================================================

Write-Host "`n🚀 Starting Backend & Frontend..." -ForegroundColor Cyan

# Start Backend in new window (skip tests to avoid compilation errors)
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\mvnw.cmd -f apps/admin-backend-spring/pom.xml spring-boot:run -DskipTests"

# Wait 3 seconds
Start-Sleep -Seconds 3

# Start Frontend in new window
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\apps\web-portal-next'; pnpm dev"

Write-Host "`n✅ Servers starting in separate windows!" -ForegroundColor Green
Write-Host "   Backend:  http://localhost:8080" -ForegroundColor Yellow
Write-Host "   Frontend: http://localhost:3000" -ForegroundColor Yellow
Write-Host "`n   Login: admin@gmail.com / password`n" -ForegroundColor Cyan
