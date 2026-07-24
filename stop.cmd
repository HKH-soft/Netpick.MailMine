@echo off
REM stop.cmd — Stop all MailMine services (Backend + Frontend).
setlocal enabledelayedexpansion

echo Stopping MailMine services...

set "FOUND=0"

REM Stop Backend (Java/Maven process)
tasklist /fi "windowtitle eq MailMine-Backend" 2>nul | findstr /i "cmd" >nul 2>&1
if !errorlevel! equ 0 (
    taskkill /fi "windowtitle eq MailMine-Backend" /t /f >nul 2>&1
    echo   [STOPPED] Backend
    set "FOUND=1"
)

REM Stop Frontend (Node.js process)
tasklist /fi "windowtitle eq MailMine-Frontend" 2>nul | findstr /i "cmd" >nul 2>&1
if !errorlevel! equ 0 (
    taskkill /fi "windowtitle eq MailMine-Frontend" /t /f >nul 2>&1
    echo   [STOPPED] Frontend
    set "FOUND=1"
)

REM Also kill orphaned Spring Boot / Next.js dev processes on known ports
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING" 2^>nul') do (
    taskkill /pid %%p /f >nul 2>&1
    echo   [STOPPED] Orphaned process on port 8080 (PID: %%p)
    set "FOUND=1"
)
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING" 2^>nul') do (
    taskkill /pid %%p /f >nul 2>&1
    echo   [STOPPED] Orphaned process on port 3000 (PID: %%p)
    set "FOUND=1"
)

if "!FOUND!"=="0" (
    echo   No MailMine services are running.
)

echo.
endlocal
exit /b 0
