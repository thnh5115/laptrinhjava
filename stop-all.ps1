# ========================================
# Carbon Credit Marketplace - Stop All Services
# ========================================

Write-Host "Stopping Carbon Credit Marketplace Services..." -ForegroundColor Yellow
Write-Host ""

Set-Location "$PSScriptRoot\infra\docker"

Write-Host "Stopping Docker Compose services..." -ForegroundColor Yellow
docker-compose down

Write-Host ""
Write-Host "[SUCCESS] All services stopped!" -ForegroundColor Green
Write-Host ""
Write-Host "To remove volumes (WARNING: This will delete all database data!):" -ForegroundColor Yellow
Write-Host "   docker-compose down -v" -ForegroundColor White
Write-Host ""
