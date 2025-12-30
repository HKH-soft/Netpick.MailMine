#!/bin/bash

# Initialize Let's Encrypt certificates using Cloudflare DNS validation
# This script obtains certificates using DNS-01 challenge (more secure, no need for HTTP)

set -e

# Configuration - use environment variables or defaults
CERTBOT_DOMAIN="${CERTBOT_DOMAIN:-netpick.ir}"
CERTBOT_DOMAIN_WWW="${CERTBOT_DOMAIN_WWW:-www.netpick.ir}"
RSA_KEY_SIZE="${RSA_KEY_SIZE:-4096}"
CERTBOT_EMAIL="${CERTBOT_EMAIL:-hossein@netpick.ir}"
DNS_PROPAGATION_SECONDS="${DNS_PROPAGATION_SECONDS:-30}"
STAGING="${STAGING:-0}" # Set to 1 for testing

domains=($CERTBOT_DOMAIN $CERTBOT_DOMAIN_WWW)
data_path="./certbot-certs"

echo "### Checking Cloudflare credentials..."
if [ ! -f "./app-config/certbot/cloudflare.ini" ]; then
  echo "ERROR: Cloudflare credentials file not found!"
  echo "Please create ./app-config/certbot/cloudflare.ini with your Cloudflare API token:"
  echo "  dns_cloudflare_api_token = YOUR_API_TOKEN_HERE"
  echo ""
  echo "See: https://certbot-dns-cloudflare.readthedocs.io/en/stable/"
  exit 1
fi

echo "### Checking if certificates already exist..."
if [ -d "$data_path/live/${domains[0]}" ]; then
  echo "Certificates already exist. Skipping certificate creation."
  echo "To force renewal, remove the certificates folder first."
  exit 0
fi

echo "### Requesting Let's Encrypt certificate using DNS-01 challenge..."
# Join $domains to -d args
domain_args=""
for domain in "${domains[@]}"; do
  domain_args="$domain_args -d $domain"
done

# Select appropriate email arg
case "$CERTBOT_EMAIL" in
  "") email_arg="--register-unsafely-without-email" ;;
  *) email_arg="--email $CERTBOT_EMAIL" ;;
esac

# Enable staging mode if needed
staging_arg=""
if [ $STAGING != "0" ]; then staging_arg="--staging"; fi

echo "Using DNS validation with Cloudflare (propagation time: ${DNS_PROPAGATION_SECONDS}s)..."
docker-compose run --rm --entrypoint "\
  certbot certonly --dns-cloudflare \
    --dns-cloudflare-credentials /scripts/cloudflare.ini \
    --dns-cloudflare-propagation-seconds $DNS_PROPAGATION_SECONDS \
    $staging_arg \
    $email_arg \
    $domain_args \
    --rsa-key-size $RSA_KEY_SIZE \
    --agree-tos \
    --non-interactive" certbot
echo

echo "### Enabling SSL configuration..."
if [ -f "app-config/nginx/conf.d/ssl.conf.disabled" ]; then
  cp app-config/nginx/conf.d/ssl.conf.disabled app-config/nginx/conf.d/ssl.conf
  echo "SSL configuration enabled."
fi

echo "### Reloading nginx with real certificates..."
docker exec nginx nginx -s reload
echo

echo "### Certificate setup complete!"
echo "### Your site should now be accessible via HTTPS"
