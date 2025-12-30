# Docker Setup Guide

## Quick Start

### 1. Configure Environment Variables

Edit the following files in `.config/docker/`:
- `backend.env` - Backend Spring Boot configuration
- `frontend.env` - Frontend Next.js configuration  
- `postgres.env` - PostgreSQL credentials
- `redis.env` - Redis password
- `grafana.env` - Grafana admin credentials

### 2. Update Domain Configuration

In `docker-compose.yml`, find the `certbot-init` service and replace:
- `yourdomain.com` with your actual domain
- `www.yourdomain.com` with your subdomain
- `you@domain.com` with your email

Also update `app-config/nginx/conf.d/default.conf` and `ssl.conf`:
- Replace `yourdomain.com` in CORS map and `server_name` directives

### 3. Start Services Without SSL (First Time)

```powershell
# Start everything except certbot (SSL not ready yet)
docker-compose up -d --scale certbot=0
```

### 4. Obtain Initial SSL Certificates

**IMPORTANT**: Point your DNS A records to your server IP first!

```powershell
# Run the one-time certificate acquisition
docker-compose --profile setup up certbot-init

# Verify certificates were created
docker volume inspect web-scrape_certbot-certs
```

### 5. Restart Nginx with SSL

```powershell
# Restart nginx to load the new certificates
docker-compose restart nginx

# Start the auto-renewal service
docker-compose up -d certbot
```

### 6. Verify Everything Works

```powershell
# Check all services are healthy
docker-compose ps

# Test HTTPS
curl -I https://yourdomain.com

# View logs
docker-compose logs -f nginx
docker-compose logs -f certbot
```

## Certificate Auto-Renewal

The `certbot` service runs continuously and checks for renewal twice daily. When certificates are renewed, it automatically reloads Nginx.

To force a renewal test:
```powershell
docker exec certbot certbot renew --dry-run
```

## Monitoring

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000

## Resource Limits

All services have CPU and memory limits configured. Adjust in `docker-compose.yml` under `deploy.resources` if needed.

## Troubleshooting

### Nginx won't start
- Check if certificates exist: `docker volume inspect web-scrape_certbot-certs`
- Temporarily comment out the `ssl.conf` include until certs are obtained

### Certbot fails
- Ensure port 80 is accessible from the internet
- Verify DNS points to your server
- Check logs: `docker-compose logs certbot-init`

### Backend health check fails
- The image needs `wget`. If using a custom image, install it or change healthcheck to use Java

## Production Checklist

- [ ] All `.env` files configured with strong passwords
- [ ] Domain DNS properly configured
- [ ] Firewall allows ports 80, 443
- [ ] SSL certificates obtained
- [ ] CORS origins configured in nginx config
- [ ] Prometheus/Grafana secured (not exposed on 0.0.0.0 in production)
- [ ] Resource limits adjusted for your hardware
- [ ] Backup strategy for volumes (pgdata, redis-data, etc.)
