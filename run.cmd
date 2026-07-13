@echo off
REM run.cmd — Run the Backend with Maven Wrapper and the chosen Spring profile.
REM
REM Usage:
REM   run.cmd              REM defaults to 'dev'
REM   run.cmd dev          REM SQLite, no Redis
REM   run.cmd pro          REM PostgreSQL + Redis (production defaults)
REM   run.cmd test         REM H2 in-memory
REM
REM Extra args after the profile are forwarded to Spring Boot, e.g.:
REM   run.cmd dev --server.port=9090

setlocal enabledelayedexpansion

set "PROFILE=%~1"
if "%PROFILE%"=="" set "PROFILE=dev"

REM Validate profile
if /I "%PROFILE%"=="dev"      goto :run
if /I "%PROFILE%"=="pro"      goto :run
if /I "%PROFILE%"=="staging"  goto :run
if /I "%PROFILE%"=="test"     goto :run
echo ERROR: Unknown profile '%PROFILE%'. Choose from: dev, pro, staging, test
exit /b 1

:run
set "BACKEND_DIR=%~dp0Backend"

echo === Starting MailMine Backend [profile: %PROFILE%] ===

REM Shift profile out, forward remaining args
shift
set "EXTRA_ARGS="
:collect_args
if "%~1"=="" goto :start
set "EXTRA_ARGS=!EXTRA_ARGS! %~1"
shift
goto :collect_args

:start
cd /d "%BACKEND_DIR%"
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=%PROFILE% !EXTRA_ARGS!
