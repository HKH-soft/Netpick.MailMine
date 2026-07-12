#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Generates secure .env files for Docker services.
.DESCRIPTION
    Creates backend.env, frontend.env, postgres.env, redis.env, grafana.env, certbot.env
    with cryptographically secure randomly generated secrets.
.EXAMPLE
    .\scripts\generate-secrets.ps1
    .\scripts\generate-secrets.ps1 -Force  # Overwrite existing .env files
#>

param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$ConfigDir = Join-Path $PSScriptRoot "..\.config\docker"

# Generate cryptographically secure random secrets
function New-SecureSecret {
    param([int]$Length = 32)
    $bytes = New-Object byte[] $Length
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    return [Convert]::ToBase64String($bytes).Substring(0, $Length)
}

function New-HexSecret {
    param([int]$Length = 32)
    $bytes = New-Object byte[] ($Length / 2)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    return ($bytes | ForEach-Object { $_.ToString("x2") }) -join ""
}

# Create directory if needed
if (-not (Test-Path $ConfigDir)) {
    New-Item -ItemType Directory -Path $ConfigDir -Force | Out-Null
}

# Generate secrets with cryptographic randomness
$JwtSecret = New-HexSecret -Length 64
$RedisPassword = New-SecureSecret -Length 48
$PostgresPassword = New-SecureSecret -Length 48
$GrafanaAdminPassword = New-SecureSecret -Length 24

Write-Host "`n=== Netpick MailMine Secret Generator ===" -ForegroundColor Cyan

# backend.env
@"
# Backend Spring Boot Configuration
DATABASE_NAME=mailmine
POSTGRES_URL=jdbc:postgresql://postgres:5432/mailmine
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=$PostgresPassword
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=$RedisPassword
JWT_SECRET_KEY=$JwtSecret
JWT_ISSUER=mailmine
SERVER_PORT=8080
XRAY_EXECUTABLE_PATH=/app/xray/xray
GEMINI_API_KEY=your_gemini_api_key_here
GMAIL_USERNAME=your_gmail@gmail.com
GMAIL_PASSWORD=your_gmail_app_password
ACTUATOR_USERNAME=admin
ACTUATOR_PASSWORD=change_this_password
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://netpick.ir
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Authorization,Content-Type,X-Requested-With
CORS_EXPOSED_HEADERS=Content-Length,Content-Type
"@ | Set-Content -Path "$ConfigDir\backend.env" -Encoding UTF8

# frontend.env
@"
# Frontend Next.js Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NODE_ENV=development
"@ | Set-Content -Path "$ConfigDir\frontend.env" -Encoding UTF8

# postgres.env
@"
# PostgreSQL Configuration
POSTGRES_USER=postgres
POSTGRES_PASSWORD=$PostgresPassword
POSTGRES_DB=mailmine
PGDATA=/var/lib/postgresql/data/pgdata
"@ | Set-Content -Path "$ConfigDir\postgres.env" -Encoding UTF8

# redis.env
@"
# Redis Configuration
REDIS_PASSWORD=$RedisPassword
"@ | Set-Content -Path "$ConfigDir\redis.env" -Encoding UTF8

# grafana.env
@"
# Grafana Configuration
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=$GrafanaAdminPassword
GF_USERS_ALLOW_SIGN_UP=false
"@ | Set-Content -Path "$ConfigDir\grafana.env" -Encoding UTF8

# certbot.env
@"
# Certbot Configuration
CERTBOT_DOMAIN=netpick.ir
CERTBOT_DOMAIN_WWW=www.netpick.ir
CERTBOT_EMAIL=admin@netpick.ir
DNS_PROPAGATION_SECONDS=30
RSA_KEY_SIZE=4096
"@ | Set-Content -Path "$ConfigDir\certbot.env" -Encoding UTF8

# alertmanager.env
@"
# Alertmanager Configuration
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_chat_id
DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/your_webhook
"@ | Set-Content -Path "$ConfigDir\alertmanager.env" -Encoding UTF8

Write-Host "`nGenerated .env files in $ConfigDir" -ForegroundColor Green
Write-Host "IMPORTANT: Update placeholder values (GEMINI_API_KEY, GMAIL_*, CERTBOT_*, TELEGRAM_*)" -ForegroundColor Yellow

# Generate Docker Swarm secrets
$SwarmDir = Join-Path $PSScriptRoot "..\.config\swarm-secrets"
if (-not (Test-Path $SwarmDir)) {
    New-Item -ItemType Directory -Path $SwarmDir -Force | Out-Null
}

Write-Host "`n=== Docker Swarm Secrets ===" -ForegroundColor Cyan

$SwarmSecrets = @(
    @{ Name = "ACTUATOR_USERNAME_secret"; Value = "admin" },
    @{ Name = "ACTUATOR_PASSWORD_secret"; Value = New-SecureSecret -Length 32 },
    @{ Name = "POSTGRES_USER_secret"; Value = "mailmine" },
    @{ Name = "POSTGRES_PASSWORD_secret"; Value = $PostgresPassword },
    @{ Name = "POSTGRES_DB_secret"; Value = "mailmine" },
    @{ Name = "REDIS_PASSWORD_secret"; Value = $RedisPassword },
    @{ Name = "JWT_SECRET_KEY_secret"; Value = $JwtSecret },
    @{ Name = "GEMINI_API_KEY_secret"; Value = "CHANGE_ME_gemini_api_key" },
    @{ Name = "GMAIL_USERNAME_secret"; Value = "CHANGE_ME_gmail_username" },
    @{ Name = "GMAIL_PASSWORD_secret"; Value = "CHANGE_ME_gmail_password" }
)

foreach ($s in $SwarmSecrets) {
    $s.Value | Set-Content -Path (Join-Path $SwarmDir $s.Name) -NoNewline -Encoding UTF8
    Write-Host "  OK    $($s.Name)" -ForegroundColor Green
}

Write-Host @"

Next steps:
  1. Review generated .env files in .config/docker/
  2. Update CHANGE_ME values (Gemini API key, Gmail credentials, domain)
  3. For production swarm, initialize secrets:
     Get-ChildItem .config\swarm-secrets\* | ForEach-Object {
       docker secret create $_.Name $_.FullName
     }
  4. Start services: docker-compose up -d
"@ -ForegroundColor DarkGray