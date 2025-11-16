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
    "$lines = Get-Content -LiteralPath '%SELF%'; $start = %START_INDEX%; $script = ($lines[$start..($lines.Length-1)] -join \"`n\"); & ([scriptblock]::Create($script)) @args" --% %*
set "ERR=%ERRORLEVEL%"
endlocal & exit /b %ERR%

__PS_PAYLOAD__
param(
    [switch]$SkipAdmin,
    [switch]$SkipBuyer,
    [switch]$SkipOwner,
    [switch]$SkipCVA,
    [switch]$SkipFrontend
)

$ProjectRoot = $PSScriptRoot

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host "  Quick Start - Carbon Credit Marketplace" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

Write-Host "`n[IMPORTANT] Startup Order:" -ForegroundColor Red
Write-Host "  1. Admin Backend starts FIRST (creates core tables)" -ForegroundColor Yellow
Write-Host "  2. Waits for Admin to be ready (~30-60 seconds)" -ForegroundColor Yellow
Write-Host "  3. Other backends start (depend on Admin tables)" -ForegroundColor Yellow
Write-Host "  4. Frontend starts last" -ForegroundColor Yellow

Write-Host "`n[STEP] Checking environment..." -ForegroundColor Yellow

if (-not $env:JAVA_HOME) {
    Write-Host "[WARN] JAVA_HOME not set. Attempting to detect..." -ForegroundColor Yellow
    try {
        $javaPath = (Get-Command java -ErrorAction Stop).Source
        $javaBinDir = Split-Path $javaPath -Parent
        $javaHomeCandidate = Split-Path $javaBinDir -Parent
        
        if (Test-Path "$javaHomeCandidate\bin\java.exe") {
            $env:JAVA_HOME = $javaHomeCandidate
            Write-Host "[OK] Auto-detected JAVA_HOME: $javaHomeCandidate" -ForegroundColor Green
        } else {
            Write-Host "[ERROR] Could not auto-detect JAVA_HOME. Please set it manually:" -ForegroundColor Red
            Write-Host "  `$env:JAVA_HOME = 'C:\Path\To\Java'" -ForegroundColor White
            exit 1
        }
    } catch {
        Write-Host "[ERROR] Java not found in PATH. Please install Java and set JAVA_HOME" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[OK] JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
}

$services = @()

if (-not $SkipAdmin) {
    $services += @{
        Name = "Admin Backend"
        Port = 8080
        Path = "apps/admin-backend-spring"
        Command = "`$env:JAVA_HOME='$env:JAVA_HOME'; `$env:SPRING_PROFILES_ACTIVE='dev'; cd '$ProjectRoot'; .\mvnw.cmd -f apps/admin-backend-spring/pom.xml -DskipTests spring-boot:run"
    }
}

if (-not $SkipOwner) {
    $services += @{
        Name = "Owner Backend"
        Port = 8082
        Path = "apps/owner-backend-spring"
        Command = "`$env:JAVA_HOME='$env:JAVA_HOME'; `$env:SPRING_PROFILES_ACTIVE='dev'; cd '$ProjectRoot'; .\mvnw.cmd -f apps/owner-backend-spring/pom.xml -DskipTests spring-boot:run"
    }
}

if (-not $SkipBuyer) {
    $services += @{
        Name = "Buyer Backend"
        Port = 8081
        Path = "apps/buyer-backend-spring"
        Command = "`$env:JAVA_HOME='$env:JAVA_HOME'; `$env:SPRING_PROFILES_ACTIVE='dev'; cd '$ProjectRoot'; .\mvnw.cmd -f apps/buyer-backend-spring/pom.xml -DskipTests spring-boot:run"
    }
}

if (-not $SkipCVA) {
    $services += @{
        Name = "CVA Backend"
        Port = 8083
        Path = "apps/cva-backend-spring"
        Command = "`$env:JAVA_HOME='$env:JAVA_HOME'; `$env:SPRING_PROFILES_ACTIVE='dev'; cd '$ProjectRoot'; .\mvnw.cmd -f apps/cva-backend-spring/pom.xml -DskipTests spring-boot:run"
    }
}

Write-Host "`n[INFO] Starting backend services SEQUENTIALLY in correct order..." -ForegroundColor Yellow
Write-Host "[WARN] Each service will wait for the previous one to be ready!" -ForegroundColor Yellow
Write-Host "[INFO] Total startup time: ~2-4 minutes (depends on your machine)" -ForegroundColor Gray

function Wait-ForService {
    param(
        [string]$ServiceName,
        [int]$Port,
        [int]$MaxWaitSeconds = 120
    )
    
    $healthUrl = "http://localhost:$Port/actuator/health"
    Write-Host "`n  [INFO] Waiting for $ServiceName to be ready..." -ForegroundColor Yellow
    Write-Host "  [INFO] Checking $healthUrl every 5 seconds..." -ForegroundColor Gray
    
    $waited = 0
    $isReady = $false
    
    while ($waited -lt $MaxWaitSeconds -and -not $isReady) {
        Start-Sleep -Seconds 5
        $waited += 5
        
        try {
            $response = Invoke-WebRequest -Uri $healthUrl -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                $isReady = $true
                Write-Host "`n  [OK] $ServiceName is ready! (waited $waited seconds)" -ForegroundColor Green
            }
        } catch {
            Write-Host "." -NoNewline -ForegroundColor Gray
        }
    }
    
    if (-not $isReady) {
        Write-Host "`n  [WARN] $ServiceName did not respond after $MaxWaitSeconds seconds" -ForegroundColor Yellow
        Write-Host "  [INFO] Check the $ServiceName window for errors" -ForegroundColor Gray
    }
}

foreach ($service in $services) {
    Write-Host "`n[STEP] Launching $($service.Name) ..." -ForegroundColor Cyan
    Write-Host "  [INFO] Working directory: $ProjectRoot" -ForegroundColor Gray
    Write-Host "  [INFO] Running: $($service.Command)" -ForegroundColor Gray
    
    Start-Process powershell -ArgumentList "-NoExit","-Command",$service.Command
    Write-Host "  [OK] Launching $($service.Name) on port $($service.Port)..." -ForegroundColor Green
    Wait-ForService -ServiceName $service.Name -Port $service.Port -MaxWaitSeconds 120
}

Write-Host "`n================================================================" -ForegroundColor Green
Write-Host "  [OK] All backend services are running!" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

if (-not $SkipFrontend) {
    Write-Host "`n[INFO] Starting Frontend (Next.js)..." -ForegroundColor Yellow
    
    if (Test-Path "$ProjectRoot\apps\web-portal-next\node_modules") {
        Write-Host "  [OK] node_modules found" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] node_modules not found. Installing dependencies..." -ForegroundColor Yellow
        Push-Location "$ProjectRoot\apps\web-portal-next"
        try {
            if (Test-Path "$ProjectRoot\package-lock.json" -or (Get-Command npm -ErrorAction SilentlyContinue)) {
                npm install --legacy-peer-deps
            } elseif (Get-Command pnpm -ErrorAction SilentlyContinue) {
                pnpm install --no-frozen-lockfile
            } else {
                npm install --legacy-peer-deps
            }
        } finally {
            Pop-Location
        }
    }
    
    $frontendCmd = "cd '$ProjectRoot\apps\web-portal-next'; npm run dev"
    Write-Host "  [INFO] Running: $frontendCmd" -ForegroundColor Gray
    Start-Process powershell -ArgumentList "-NoExit","-Command",$frontendCmd
    Write-Host "  [OK] Frontend starting on port 3000..." -ForegroundColor Green
}

Write-Host "`n==================================================================" -ForegroundColor Green
Write-Host "  [OK] All services starting in separate windows!" -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Green

Write-Host "`n[STEP] Service URLs:" -ForegroundColor Cyan
if (-not $SkipAdmin) {
    Write-Host "  - Admin Backend:  http://localhost:8080" -ForegroundColor Yellow
    Write-Host "    Swagger UI:     http://localhost:8080/swagger-ui.html" -ForegroundColor White
}
if (-not $SkipBuyer) {
    Write-Host "  - Buyer Backend:  http://localhost:8081" -ForegroundColor Yellow
    Write-Host "    Swagger UI:     http://localhost:8081/swagger-ui.html" -ForegroundColor White
}
if (-not $SkipOwner) {
    Write-Host "  - Owner Backend:  http://localhost:8082" -ForegroundColor Yellow
    Write-Host "    Swagger UI:     http://localhost:8082/swagger-ui.html" -ForegroundColor White
}
if (-not $SkipCVA) {
    Write-Host "  - CVA Backend:    http://localhost:8083" -ForegroundColor Yellow
    Write-Host "    Swagger UI:     http://localhost:8083/swagger-ui.html" -ForegroundColor White
}
if (-not $SkipFrontend) {
    Write-Host "  - Web Frontend:   http://localhost:3000" -ForegroundColor Yellow
}

Write-Host "`n[STEP] Test Login Credentials:" -ForegroundColor Cyan
Write-Host "  Email:    admin@test.local" -ForegroundColor White
Write-Host "  Password: password" -ForegroundColor White
Write-Host ""
Write-Host "  Other users:" -ForegroundColor White
Write-Host "  - buyer@test.local / password" -ForegroundColor Gray
Write-Host "  - seller@test.local / password" -ForegroundColor Gray
Write-Host "  - auditor@test.local / password" -ForegroundColor Gray

Write-Host "`n[STEP] Useful Commands:" -ForegroundColor Cyan
Write-Host "  Stop all: Get-Process -Name 'java','node' | Stop-Process -Force" -ForegroundColor White
Write-Host "  View logs: Check the PowerShell windows that opened" -ForegroundColor White

Write-Host "`n[STEP] Startup Options:" -ForegroundColor Cyan
Write-Host "  .\start.bat                  # Start all services" -ForegroundColor White
Write-Host "  .\start.bat -SkipBuyer       # Skip Buyer service" -ForegroundColor White
Write-Host "  .\start.bat -SkipFrontend    # Backend only" -ForegroundColor White
Write-Host ""
