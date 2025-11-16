@echo off
setlocal EnableDelayedExpansion
set "SELF=%~f0"
set "MARKER=__PS_PAYLOAD__"
for /f "delims=:" %%I in ('findstr /n /c:"%MARKER%" "%SELF%"') do set "PAYLOAD_LINE=%%I"
if not defined PAYLOAD_LINE (
    echo [ERROR] Unable to locate embedded PowerShell payload in %SELF%.
    endlocal & exit /b 1
)
set /a START_INDEX=PAYLOAD_LINE
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$lines = Get-Content -LiteralPath '%SELF%'; $start = %START_INDEX%; $script = ($lines[$start..($lines.Length-1)] -join \"`n\"); & ([scriptblock]::Create($script)) @args"
set "ERR=%ERRORLEVEL%"
endlocal & exit /b %ERR%

__PS_PAYLOAD__
Write-Host "Stopping all projects..." -ForegroundColor Red

function Stop-ProcessByPort {
    param([int]$Port)
    $process = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    if ($process) {
        $proc = Get-Process -Id $process -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "Stopping process $($proc.Name) (PID: $($proc.Id)) on port $Port..." -ForegroundColor Yellow
            Stop-Process -Id $process -Force
        }
    }
}

Write-Host "Stopping Spring Boot backends..." -ForegroundColor Cyan
Stop-ProcessByPort 8080
Stop-ProcessByPort 8081
Stop-ProcessByPort 8082
Stop-ProcessByPort 8083

Write-Host "Stopping Next.js development server..." -ForegroundColor Cyan
Stop-ProcessByPort 3000
Stop-ProcessByPort 3001

Write-Host "Stopping Docker containers..." -ForegroundColor Cyan
if (Test-Path "infra/docker/docker-compose.yml") {
    Push-Location "infra/docker"
    try {
        docker compose down
        Write-Host "Docker containers stopped successfully." -ForegroundColor Green
    } catch {
        Write-Host "Failed to stop Docker containers: $($_.Exception.Message)" -ForegroundColor Red
    }
    Pop-Location
} else {
    Write-Host "Docker compose file not found at infra/docker/docker-compose.yml" -ForegroundColor Yellow
}

Write-Host "Checking for remaining Java processes..." -ForegroundColor Cyan
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "Found $($javaProcesses.Count) Java process(es). Stopping..." -ForegroundColor Yellow
    $javaProcesses | Stop-Process -Force
} else {
    Write-Host "No Java processes found." -ForegroundColor Green
}

Write-Host "Checking for remaining Node.js processes..." -ForegroundColor Cyan
$nodeProcesses = Get-Process node -ErrorAction SilentlyContinue
if ($nodeProcesses) {
    Write-Host "Found $($nodeProcesses.Count) Node.js process(es). Stopping..." -ForegroundColor Yellow
    $nodeProcesses | Stop-Process -Force
} else {
    Write-Host "No Node.js processes found." -ForegroundColor Green
}

Write-Host "All projects stopped successfully!" -ForegroundColor Green
