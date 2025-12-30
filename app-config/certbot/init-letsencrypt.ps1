# Initialize Let's Encrypt certificates using Cloudflare DNS validation (PowerShell version)
# This script obtains certificates using DNS-01 challenge (more secure, no need for HTTP)

$ErrorActionPreference = "Stop"

# Configuration - use environment variables or defaults
$certbotDomain = if ($env:CERTBOT_DOMAIN) { $env:CERTBOT_DOMAIN } else { "netpick.ir" }
$certbotDomainWww = if ($env:CERTBOT_DOMAIN_WWW) { $env:CERTBOT_DOMAIN_WWW } else { "www.netpick.ir" }
$rsaKeySize = if ($env:RSA_KEY_SIZE) { $env:RSA_KEY_SIZE } else { 4096 }
$email = if ($env:CERTBOT_EMAIL) { $env:CERTBOT_EMAIL } else { "hossein@netpick.ir" }
$dnsPropagationSeconds = if ($env:DNS_PROPAGATION_SECONDS) { $env:DNS_PROPAGATION_SECONDS } else { 30 }
$staging = if ($env:STAGING) { $env:STAGING } else { 0 }

$domains = @($certbotDomain, $certbotDomainWww)
$dataPath = "./certbot-certs"

Write-Host "### Checking Cloudflare credentials..." -ForegroundColor Green
if (-not (Test-Path "./app-config/certbot/cloudflare.ini")) {
    Write-Host "ERROR: Cloudflare credentials file not found!" -ForegroundColor Red
    Write-Host "Please create ./app-config/certbot/cloudflare.ini with your Cloudflare API token:" -ForegroundColor Yellow
    Write-Host "  dns_cloudflare_api_token = YOUR_API_TOKEN_HERE" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "See: https://certbot-dns-cloudflare.readthedocs.io/en/stable/" -ForegroundColor Yellow
    exit 1
}

Write-Host "### Checking if certificates already exist..." -ForegroundColor Green
if (Test-Path "$dataPath/live/$($domains[0])") {
    Write-Host "Certificates already exist. Skipping certificate creation." -ForegroundColor Yellow
    Write-Host "To force renewal, remove the certificates folder first." -ForegroundColor Yellow
    exit 0
}

Write-Host "`n### Requesting Let's Encrypt certificate using DNS-01 challenge..." -ForegroundColor Green
# Join domains to -d args
$domainArgs = ""
foreach ($domain in $domains) {
    $domainArgs += " -d $domain"
}

# Select appropriate email arg
if ([string]::IsNullOrEmpty($email)) {
    $emailArg = "--register-unsafely-without-email"
} else {
    $emailArg = "--email $email"
}

# Enable staging mode if needed
$stagingArg = ""
if ($staging -ne 0) {
    $stagingArg = "--staging"
}

Write-Host "Using DNS validation with Cloudflare (propagation time: $($dnsPropagationSeconds)s)..." -ForegroundColor Cyan
docker-compose run --rm --entrypoint "certbot certonly --dns-cloudflare --dns-cloudflare-credentials /scripts/cloudflare.ini --dns-cloudflare-propagation-seconds $dnsPropagationSeconds $stagingArg $emailArg $domainArgs --rsa-key-size $rsaKeySize --agree-tos --non-interactive" certbot
Write-Host ""

Write-Host "### Enabling SSL configuration..." -ForegroundColor Green
if (Test-Path "app-config/nginx/conf.d/ssl.conf.disabled") {
    Copy-Item "app-config/nginx/conf.d/ssl.conf.disabled" "app-config/nginx/conf.d/ssl.conf" -Force
    Write-Host "SSL configuration enabled." -ForegroundColor Green
}

Write-Host "### Reloading nginx with real certificates..." -ForegroundColor Green
docker exec nginx nginx -s reload
Write-Host ""

Write-Host "### Certificate setup complete!" -ForegroundColor Cyan
Write-Host "### Your site should now be accessible via HTTPS" -ForegroundColor Cyan
