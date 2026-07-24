@echo off
REM ============================================================================
REM start.cmd — Start MailMine Backend + Frontend simultaneously.
REM
REM Usage:
REM   start.cmd                REM start both services with default profile
REM   start.cmd --backend-only REM start only the backend
REM   start.cmd --frontend-onlyREM start only the frontend
REM   start.cmd --profile pro  REM use a specific Spring profile for the backend
REM ============================================================================
setlocal enabledelayedexpansion

REM ── Parse arguments ─────────────────────────────────────────────────────────
set "START_BACKEND=1"
set "START_FRONTEND=1"
set "PROFILE=dev"
set "EXTRA_ARGS="

:parse_args
if "%~1"=="" goto :validate_args
if /I "%~1"=="--backend-only"  ( set "START_BACKEND=1" & set "START_FRONTEND=0" & shift & goto :parse_args )
if /I "%~1"=="--frontend-only" ( set "START_BACKEND=0" & set "START_FRONTEND=1" & shift & goto :parse_args )
if /I "%~1"=="--profile"       ( set "PROFILE=%~2" & shift & shift & goto :parse_args )
set "EXTRA_ARGS=!EXTRA_ARGS! %~1"
shift
goto :parse_args

:validate_args
if "%START_BACKEND%"=="0" if "%START_FRONTEND%"=="0" (
    echo ERROR: Cannot disable both services.
    exit /b 1
)

REM ── Validate profile ────────────────────────────────────────────────────────
if /I "%PROFILE%"=="dev"      goto :check_env
if /I "%PROFILE%"=="pro"      goto :check_env
if /I "%PROFILE%"=="staging"  goto :check_env
if /I "%PROFILE%"=="test"     goto :check_env
echo ERROR: Unknown profile '%PROFILE%'. Choose from: dev, pro, staging, test
exit /b 1

:check_env
REM ── Check prerequisites ────────────────────────────────────────────────────
set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%Backend"
set "FRONTEND_DIR=%ROOT_DIR%Frontend"

if "%START_BACKEND%"=="1" (
    if not exist "%BACKEND_DIR%\mvnw.cmd" (
        echo ERROR: Backend directory or mvnw.cmd not found at "%BACKEND_DIR%"
        exit /b 1
    )
)

if "%START_FRONTEND%"=="1" (
    if not exist "%FRONTEND_DIR%\package.json" (
        echo ERROR: Frontend directory not found at "%FRONTEND_DIR%"
        exit /b 1
    )
    where npm >nul 2>&1
    if errorlevel 1 (
        echo ERROR: npm not found in PATH. Install Node.js first.
        exit /b 1
    )
    if not exist "%FRONTEND_DIR%\node_modules" (
        echo === Installing frontend dependencies... ===
        pushd "%FRONTEND_DIR%"
        call npm install
        popd
        if errorlevel 1 (
            echo ERROR: npm install failed.
            exit /b 1
        )
    )
)

REM ── Display banner ─────────────────────────────────────────────────────────
echo.
echo   ╔═══════════════════════════════════════════════╗
echo   ║           MailMine — Development Start        ║
echo   ╠═══════════════════════════════════════════════╣
if "%START_BACKEND%"=="1"  echo   ║  Backend  : http://localhost:8080  [profile: %PROFILE%] ║
if "%START_FRONTEND%"=="1" echo   ║  Frontend : http://localhost:3000  [Next.js dev]     ║
echo   ╚═══════════════════════════════════════════════╝
echo.

REM ── Launch services in separate windows ─────────────────────────────────────
set "BACKEND_TITLE=MailMine-Backend"
set "FRONTEND_TITLE=MailMine-Frontend"

if "%START_BACKEND%"=="1" (
    echo Starting backend...
    start "%BACKEND_TITLE%" cmd /k "title %BACKEND_TITLE% && cd /d "%BACKEND_DIR%" && call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=%PROFILE% !EXTRA_ARGS! && pause"
)

if "%START_FRONTEND%"=="1" (
    echo Starting frontend...
    start "%FRONTEND_TITLE%" cmd /k "title %FRONTEND_TITLE% && cd /d "%FRONTEND_DIR%" && call npm run dev && pause"
)

echo.
echo Both services are starting in separate windows.
echo Close the service windows to stop them, or run:
echo   stop.cmd
echo.

REM ── Wait for services to be ready ──────────────────────────────────────────
set "BACKEND_READY=0"
set "FRONTEND_READY=0"
set "WAIT_COUNT=0"
set "MAX_WAIT=60"

if "%START_BACKEND%"=="0" set "BACKEND_READY=1"
if "%START_FRONTEND%"=="0" set "FRONTEND_READY=1"

:wait_loop
if "!BACKEND_READY!!FRONTEND_READY!"=="11" goto :ready
if !WAIT_COUNT! geq %MAX_WAIT% goto :ready

timeout /t 1 /nobreak >nul
set /a WAIT_COUNT+=1

if "%BACKEND_READY%"=="0" (
    curl -s -o nul -w "%%{http_code}" http://localhost:8080/actuator/health -u admin:devpassword 2>nul | findstr /r "^[234]" >nul 2>&1
    if !errorlevel! equ 0 (
        echo   [OK] Backend is ready on port 8080
        set "BACKEND_READY=1"
    )
)

if "%FRONTEND_READY%"=="0" (
    curl -s -o nul -w "%%{http_code}" http://localhost:3000 2>nul | findstr /r "^[234]" >nul 2>&1
    if !errorlevel! equ 0 (
        echo   [OK] Frontend is ready on port 3000
        set "FRONTEND_READY=1"
    )
)

goto :wait_loop

:ready
if !WAIT_COUNT! geq %MAX_WAIT% (
    echo.
    echo WARNING: Timed out waiting for services. They may still be starting.
    echo Check the service windows for status.
)
echo.
echo === MailMine is running. Close the service windows to stop. ===
echo.
endlocal
exit /b 0
