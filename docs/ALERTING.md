# Alerting Configuration

This document describes the monitoring and alerting setup using Prometheus, Alertmanager, and Grafana.

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Backend    │────▶│  Prometheus  │────▶│ Alertmanager│
│ (Spring Boot)│     │              │     │             │
└─────────────┘     └──────────────┘     └──────┬──────┘
       │                   │                     │
       ▼                   ▼                     ▼
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Redis     │     │   Grafana   │◀───▶│ Telegram    │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   Discord   │
                    └─────────────┘
```

## Alert Rules (Prometheus)

Alerts are defined in `app-config/prometheus/alert_rules.yml`:

### Backend Alerts
| Alert | Condition | Severity | Description |
|-------|-----------|----------|-------------|
| BackendDown | `up{job="backend"} == 0` for 1m | critical | Backend service unreachable |
| HighErrorRate | 5xx rate > 5% for 5m | warning | High HTTP error rate |
| HighResponseTime | p95 latency > 2s for 5m | warning | Slow responses |
| HighMemoryUsage | JVM heap > 85% for 5m | warning | Memory pressure |
| HighDatabaseConnections | Active connections > 8 for 5m | warning | Potential connection leak |
| RedisConnectionFailure | Redis command failures > 0 for 2m | critical | Redis connectivity issues |

### Infrastructure Alerts
| Alert | Condition | Severity | Description |
|-------|-----------|----------|-------------|
| PostgresDown | `up{job="postgres"} == 0` for 1m | critical | PostgreSQL unavailable |
| PrometheusScrapeFailed | Scrape failure for 5m | warning | Prometheus cannot scrape metrics |
| WebsiteDown | HTTP probe fails for 2m | critical | Site endpoint down |
| SSLCertExpiringSoon | Cert expires < 14 days | warning | SSL renewal needed |

## Notification Channels

### Telegram (Critical Alerts)
1. Create bot with [@BotFather](https://t.me/BotFather)
2. Get bot token and chat ID
3. Add to `grafana.env`:
   ```
   TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
   TELEGRAM_CHAT_ID=123456789
   ```

### Discord (All Alerts)
1. Create webhook in Discord channel
2. Add to `grafana.env`:
   ```
   DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/...
   ```

## Environment Setup

1. Copy example files:
   ```bash
   cp .config/docker/grafana.env.example .config/docker/grafana.env
   cp .config/docker/alertmanager.env.example .config/docker/alertmanager.env
   ```

2. Fill in real values in `.config/docker/grafana.env` and `.config/docker/alertmanager.env`

3. Start services:
   ```bash
   docker-compose up -d
   ```

## Access Points

| Service | URL | Purpose |
|---------|-----|---------|
| Grafana | http://localhost:3001 | Dashboards & alert management |
| Prometheus | http://localhost:9090 | Metrics & alert rules |
| Alertmanager | http://localhost:9093 | Alert routing & silences |

## Alert Inhibition

Critical alerts suppress related warnings:
- `BackendDown` inhibits `HighErrorRate` and `HighResponseTime` for same service
- `PostgresDown` inhibits database-related warnings

## Testing Alerts

Force a test alert by:
```bash
# Stop backend to trigger BackendDown
docker-compose stop backend

# Check alert fired
curl http://localhost:9090/api/v1/rules
```