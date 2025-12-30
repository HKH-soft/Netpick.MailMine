# Cloudflare DNS-01 Challenge Setup Guide

## Overview
This setup uses DNS-01 challenge validation with Cloudflare, which is more secure than HTTP-01 (webroot) validation because:
- Works without exposing your server to the internet during initial setup
- No need for port 80 to be accessible
- Can generate wildcard certificates
- More secure as it doesn't rely on HTTP validation files

## Prerequisites
1. Domain registered and using Cloudflare nameservers
2. Cloudflare account with access to your domain
3. Docker and docker-compose installed

## Setup Steps

### 1. Create Cloudflare API Token
1. Go to https://dash.cloudflare.com/profile/api-tokens
2. Click "Create Token"
3. Use the "Edit zone DNS" template
4. Configure:
   - **Permissions**: Zone → DNS → Edit
   - **Zone Resources**: Include → Specific zone → [Your Domain]
5. Click "Continue to summary" → "Create Token"
6. **Copy the token** (you won't see it again!)

### 2. Configure Cloudflare Credentials
```bash
# Copy the example file
cp app-config/certbot/cloudflare.ini.example app-config/certbot/cloudflare.ini

# Edit and add your API token
# Replace YOUR_CLOUDFLARE_API_TOKEN_HERE with your actual token
```

**Security Note**: Set proper permissions on this file:
```bash
chmod 600 app-config/certbot/cloudflare.ini
```

Add to `.gitignore`:
```
app-config/certbot/cloudflare.ini
```

### 3. Configure Environment Variables
```bash
# Copy the example file
cp .config/docker/certbot.env.example .config/docker/certbot.env

# Edit with your domain and email
# Update CERTBOT_DOMAIN, CERTBOT_EMAIL, etc.
```

### 4. Test with Staging (Recommended)
First, test with Let's Encrypt staging to avoid rate limits:

```bash
# In certbot.env, set:
STAGING=1

# Run the first-time setup
docker-compose --profile setup run certbot-init
```

If successful, switch to production:
```bash
# In certbot.env, set:
STAGING=0

# Remove test certificates
rm -rf certbot-certs/live/*
rm -rf certbot-certs/archive/*
rm -rf certbot-certs/renewal/*

# Run again for real certificates
docker-compose --profile setup run certbot-init
```

### 5. Alternative: Use Init Scripts
For Linux/macOS:
```bash
cd /path/to/project
./app-config/certbot/init-letsencrypt.sh
```

For Windows (PowerShell):
```powershell
cd \path\to\project
.\app-config\certbot\init-letsencrypt.ps1
```

## Auto-Renewal
The certbot service automatically renews certificates every 12 hours using the DNS-01 challenge. No manual intervention needed!

## Troubleshooting

### "DNS propagation failed"
- Increase `DNS_PROPAGATION_SECONDS` in certbot.env (try 60 or 90)
- Verify your Cloudflare API token has DNS:Edit permissions
- Check that your domain is using Cloudflare nameservers

### "Invalid credentials"
- Verify the API token in cloudflare.ini is correct
- Ensure the token has permissions for your specific zone
- Check token hasn't expired

### Rate Limits
Let's Encrypt has rate limits:
- 50 certificates per registered domain per week
- Use STAGING=1 for testing to avoid hitting limits

## Security Best Practices

1. **Never commit cloudflare.ini to git**
   - Add it to .gitignore immediately
   
2. **Use API tokens, not Global API Key**
   - Tokens can be scoped to specific permissions
   - Can be revoked individually
   
3. **Rotate tokens regularly**
   - Update token every 90 days
   
4. **Limit token scope**
   - Only give DNS:Edit permission
   - Only for specific zones needed

5. **Set proper file permissions**
   ```bash
   chmod 600 app-config/certbot/cloudflare.ini
   ```

## Files Structure
```
app-config/certbot/
├── cloudflare.ini.example       # Template for credentials
├── cloudflare.ini              # YOUR CREDENTIALS (git-ignored)
├── init-letsencrypt.sh         # Linux/macOS setup script
└── init-letsencrypt.ps1        # Windows setup script

.config/docker/
└── certbot.env                 # Certbot configuration
```
