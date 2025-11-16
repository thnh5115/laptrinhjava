@echo off
setlocal EnableExtensions EnableDelayedExpansion

for /f "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"
if defined ESC (
    set "TAG_INFO=%ESC%[36m[INFO]%ESC%[0m"
    set "TAG_WARN=%ESC%[33m[WARN]%ESC%[0m"
    set "TAG_ERROR=%ESC%[31m[ERROR]%ESC%[0m"
    set "TAG_SUCCESS=%ESC%[32m[SUCCESS]%ESC%[0m"
) else (
    set "TAG_INFO=[INFO]"
    set "TAG_WARN=[WARN]"
    set "TAG_ERROR=[ERROR]"
    set "TAG_SUCCESS=[SUCCESS]"
)

goto :main

:: ------------------------
:: Helper functions
:: ------------------------
:log_info
echo %TAG_INFO% %~1
exit /b 0

:log_warn
echo %TAG_WARN% %~1
exit /b 0

:log_error
echo %TAG_ERROR% %~1
exit /b 0

:log_success
echo %TAG_SUCCESS% %~1
exit /b 0

:require_command
where %~1 >nul 2>&1 || (
    call :log_error "%~2 ('%~1') is not available on PATH."
    exit /b 1
)
exit /b 0

:determine_versions
set "REQUIRED_JAVA_VERSION="
for /f "usebackq delims=" %%J in (`powershell -NoProfile -Command "[xml]$pom = Get-Content -Path 'pom.xml'; $pom.project.properties.'java.version'"`) do (
    set "REQUIRED_JAVA_VERSION=%%J"
)
if not defined REQUIRED_JAVA_VERSION set "REQUIRED_JAVA_VERSION=21"
call :log_info "Backend requires Java >= %REQUIRED_JAVA_VERSION%"

set "NEXT_DEP_VERSION="
for /f "usebackq delims=" %%N in (`powershell -NoProfile -Command "$pkg = Get-Content -Raw -Path 'apps/web-portal-next/package.json' | ConvertFrom-Json; if($pkg.dependencies -and $pkg.dependencies.next){Write-Output $pkg.dependencies.next}"`) do (
    set "NEXT_DEP_VERSION=%%N"
)
if defined NEXT_DEP_VERSION (
    call :log_info "Frontend uses Next.js %NEXT_DEP_VERSION%"
) else (
    call :log_warn "Unable to detect Next.js version."
)
exit /b 0

:check_environment
call :log_info "Checking installed tooling..."
call :require_command java "Java" || exit /b 1
call :require_command mvn "Maven" || exit /b 1
call :require_command node "Node.js" || exit /b 1
call :require_command npm "npm" || exit /b 1
call :require_command docker "Docker" || exit /b 1

for /f "tokens=2 delims==" %%J in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.version"') do (
    set "JAVA_INSTALLED_VERSION=%%J"
    goto :java_done
)
:java_done
set "JAVA_INSTALLED_VERSION=%JAVA_INSTALLED_VERSION: =%"
if defined JAVA_INSTALLED_VERSION call :log_info "Java detected: %JAVA_INSTALLED_VERSION%"

for /f "tokens=2 delims=v" %%N in ('node --version') do (
    set "NODE_INSTALLED_VERSION=%%N"
    goto :node_done
)
:node_done
if defined NODE_INSTALLED_VERSION call :log_info "Node.js detected: %NODE_INSTALLED_VERSION%"

docker info >nul 2>&1 || (
    call :log_error "Docker daemon is not reachable. Please start Docker Desktop."
    exit /b 1
)
docker compose version >nul 2>&1 || (
    call :log_error "docker compose command is not available."
    exit /b 1
)
exit /b 0

:ensure_env_file
if not exist "%ENV_DIR%" (
    call :log_error "Missing directory %ENV_DIR%. Unable to proceed."
    exit /b 1
)
if exist "%ENV_FILE%" (
    call :log_info "Environment file %ENV_FILE% already exists."
) else (
    if exist "%ENV_EXAMPLE%" (
        call :log_info "Creating %ENV_FILE% from .env.example."
        copy "%ENV_EXAMPLE%" "%ENV_FILE%" >nul || (
            call :log_error "Failed to copy %ENV_EXAMPLE%."
            exit /b 1
        )
    ) else (
        call :log_warn "No .env.example found. Writing default environment configuration."
        (
            echo MYSQL_ROOT_PASSWORD=123456
            echo MYSQL_DATABASE=ccm
            echo MYSQL_PORT=3306
            echo PHPMYADMIN_PORT=8090
            echo ADMIN_BACKEND_PORT=8080
            echo BUYER_BACKEND_PORT=8081
            echo OWNER_BACKEND_PORT=8082
            echo CVA_BACKEND_PORT=8083
            echo FRONTEND_PORT=3000
            echo NEXT_PUBLIC_API_URL=http://localhost:8080
            echo NEXT_PUBLIC_BUYER_API_URL=http://localhost:8081
            echo NEXT_PUBLIC_OWNER_API_URL=http://localhost:8082
            echo NEXT_PUBLIC_CVA_API_URL=http://localhost:8083
            echo JWT_SECRET=my-super-secret-key-for-jwt-minimum-32-characters-long-for-hs256
            echo JWT_EXPIRATION_MS=900000
            echo JWT_REFRESH_EXPIRATION_MS=604800000
        )>"%ENV_FILE%" || (
            call :log_error "Failed to create %ENV_FILE%."
            exit /b 1
        )
    )
    call :log_success "%ENV_FILE% created."
)
exit /b 0

:load_env_ports
set "MYSQL_PORT=3306"
set "PHPMYADMIN_PORT=8090"
set "ADMIN_BACKEND_PORT=8080"
set "BUYER_BACKEND_PORT=8081"
set "OWNER_BACKEND_PORT=8082"
set "CVA_BACKEND_PORT=8083"
set "FRONTEND_PORT=3000"
set "NEXT_PUBLIC_API_URL=http://localhost:8080"
set "NEXT_PUBLIC_BUYER_API_URL=http://localhost:8081"
set "NEXT_PUBLIC_OWNER_API_URL=http://localhost:8082"
set "NEXT_PUBLIC_CVA_API_URL=http://localhost:8083"
set "MYSQL_DATABASE=ccm"

if exist "%ENV_FILE%" (
    for /f "usebackq tokens=1* delims==" %%A in (`type "%ENV_FILE%" ^| findstr /r /v "^[ ]*[#;]"`) do (
        set "key=%%~A"
        set "value=%%~B"
        if /i "!key!"=="MYSQL_PORT" set "MYSQL_PORT=!value!"
        if /i "!key!"=="PHPMYADMIN_PORT" set "PHPMYADMIN_PORT=!value!"
        if /i "!key!"=="ADMIN_BACKEND_PORT" set "ADMIN_BACKEND_PORT=!value!"
        if /i "!key!"=="BUYER_BACKEND_PORT" set "BUYER_BACKEND_PORT=!value!"
        if /i "!key!"=="OWNER_BACKEND_PORT" set "OWNER_BACKEND_PORT=!value!"
        if /i "!key!"=="CVA_BACKEND_PORT" set "CVA_BACKEND_PORT=!value!"
        if /i "!key!"=="FRONTEND_PORT" set "FRONTEND_PORT=!value!"
        if /i "!key!"=="NEXT_PUBLIC_API_URL" set "NEXT_PUBLIC_API_URL=!value!"
        if /i "!key!"=="NEXT_PUBLIC_BUYER_API_URL" set "NEXT_PUBLIC_BUYER_API_URL=!value!"
        if /i "!key!"=="NEXT_PUBLIC_OWNER_API_URL" set "NEXT_PUBLIC_OWNER_API_URL=!value!"
        if /i "!key!"=="NEXT_PUBLIC_CVA_API_URL" set "NEXT_PUBLIC_CVA_API_URL=!value!"
        if /i "!key!"=="MYSQL_DATABASE" set "MYSQL_DATABASE=!value!"
    )
)

call :log_info "Ports in use: DB %MYSQL_PORT%, Admin %ADMIN_BACKEND_PORT%, Buyer %BUYER_BACKEND_PORT%, Owner %OWNER_BACKEND_PORT%, CVA %CVA_BACKEND_PORT%, Frontend %FRONTEND_PORT%, phpMyAdmin %PHPMYADMIN_PORT%"
exit /b 0

:: ------------------------
:: Main workflow
:: ------------------------
:main
set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%" >nul 2>&1 || (
    echo %TAG_ERROR% Unable to access repository directory "%SCRIPT_DIR%".
    exit /b 1
)
set "DID_PUSHD=1"

set "ENV_DIR=infra\docker"
set "ENV_FILE=%ENV_DIR%\.env"
set "ENV_EXAMPLE=%ENV_DIR%\.env.example"
set "DOCKER_COMPOSE_DIR=%ENV_DIR%"
set "DOCKER_COMPOSE_FILE=%DOCKER_COMPOSE_DIR%\docker-compose.yml"
set "EXIT_CODE=0"

call :log_info "Working directory: %SCRIPT_DIR%"

call :determine_versions
if errorlevel 1 goto :error
call :check_environment
if errorlevel 1 goto :error
call :ensure_env_file
if errorlevel 1 goto :error
call :load_env_ports
if errorlevel 1 goto :error
if not exist "%DOCKER_COMPOSE_FILE%" (
    call :log_error "Missing %DOCKER_COMPOSE_FILE%."
    goto :error
)
pushd "%DOCKER_COMPOSE_DIR%" >nul 2>&1 || (
    call :log_error "Unable to enter %DOCKER_COMPOSE_DIR%."
    goto :error
)
call :log_info "Running docker compose build..."
docker compose build
if errorlevel 1 (
    popd >nul 2>&1
    call :log_error "docker compose build failed."
    goto :error
)
call :log_info "Starting services with docker compose up -d..."
docker compose up -d
if errorlevel 1 (
    popd >nul 2>&1
    call :log_error "docker compose up failed."
    goto :error
)
popd >nul 2>&1
call :log_success "Docker Compose stack is up."

echo.
echo ===================== ACCESS INFORMATION =====================
echo  Admin Backend API : http://localhost:%ADMIN_BACKEND_PORT%
echo  Buyer Backend API : http://localhost:%BUYER_BACKEND_PORT%
echo  Owner Backend API : http://localhost:%OWNER_BACKEND_PORT%
echo  CVA Backend API   : http://localhost:%CVA_BACKEND_PORT%
echo  Frontend UI       : http://localhost:%FRONTEND_PORT%
echo  phpMyAdmin        : http://localhost:%PHPMYADMIN_PORT%
echo  MySQL             : localhost:%MYSQL_PORT%  ^(database %MYSQL_DATABASE%)
echo  NEXT_PUBLIC_API_URL      : %NEXT_PUBLIC_API_URL%
echo  NEXT_PUBLIC_BUYER_API_URL: %NEXT_PUBLIC_BUYER_API_URL%
echo  NEXT_PUBLIC_OWNER_API_URL: %NEXT_PUBLIC_OWNER_API_URL%
echo  NEXT_PUBLIC_CVA_API_URL  : %NEXT_PUBLIC_CVA_API_URL%
echo ==============================================================
echo.

call :log_success "Setup completed."
goto :cleanup

:error
set "EXIT_CODE=1"

:cleanup
if defined DID_PUSHD (
    popd >nul 2>&1
)
set "FINAL_EXIT_CODE=%EXIT_CODE%"
endlocal & exit /b %FINAL_EXIT_CODE%
