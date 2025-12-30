#!/bin/sh
set -e

# Install openssl if not present (for Alpine)
if ! command -v openssl > /dev/null 2>&1; then
    echo "Installing openssl..."
    apk add --no-cache openssl
fi

# Certificate paths (prefer Let's Encrypt location)
CERT_DIR_LETS="/etc/letsencrypt/live/netpick.ir"
CERT_DIR_FALLBACK="/etc/nginx/certs/live/netpick.ir"
CERT_FILE_LETS="$CERT_DIR_LETS/fullchain.pem"
KEY_FILE_LETS="$CERT_DIR_LETS/privkey.pem"
CERT_FILE_FALLBACK="$CERT_DIR_FALLBACK/fullchain.pem"
KEY_FILE_FALLBACK="$CERT_DIR_FALLBACK/privkey.pem"

echo "Checking for SSL certificates..."

# Prefer real certs from /etc/letsencrypt
if [ -f "$CERT_FILE_LETS" ] && [ -f "$KEY_FILE_LETS" ]; then
    echo "Let's Encrypt certificates found in $CERT_DIR_LETS. Using those."
else
    # If letsencrypt dir is writable, create self-signed there (so nginx can load them)
    if [ -w "/etc/letsencrypt" ] || [ ! -d "/etc/letsencrypt" ]; then
        echo "Let's Encrypt certs missing. Creating self-signed certificates in /etc/letsencrypt..."
        mkdir -p "$CERT_DIR_LETS"
        openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
            -keyout "$KEY_FILE_LETS" \
            -out "$CERT_FILE_LETS" \
            -subj "/CN=localhost" 2>/dev/null || true
        echo "Self-signed certificates created at $CERT_DIR_LETS. You can now obtain real certificates."
        echo "Run: docker-compose run --rm certbot-init"
    else
        # Fallback to creating self-signed in /etc/nginx/certs if /etc/letsencrypt is read-only
        echo "/etc/letsencrypt is not writable. Creating self-signed certificates in $CERT_DIR_FALLBACK..."
        mkdir -p "$CERT_DIR_FALLBACK"
        openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
            -keyout "$KEY_FILE_FALLBACK" \
            -out "$CERT_FILE_FALLBACK" \
            -subj "/CN=localhost" 2>/dev/null || true
        echo "Self-signed certificates created at $CERT_DIR_FALLBACK."
        echo "Note: nginx is configured to use /etc/letsencrypt; if certs are generated there, it will pick them up." 
    fi
fi

# Start nginx
echo "Starting nginx..."
exec nginx -g 'daemon off;'
