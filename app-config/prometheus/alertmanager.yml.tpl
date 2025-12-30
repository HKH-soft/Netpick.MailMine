global:
  resolve_timeout: 5m

# Root routing tree
route:
  receiver: "default"
  group_by: ["alertname", "service"]
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h

  routes:
    # Critical alerts
    - matchers:
        - severity="critical"
      receiver: "critical-alerts"

    # Warning alerts
    - matchers:
        - severity="warning"
      receiver: "warning-alerts"

# Define notification receivers
receivers:

  # Default receiver (fallback)
  - name: "default"
    # Typical default receiver logs to webhook or discards alerts
    webhook_configs:
      - url: "http://localhost:9093/webhook"
        send_resolved: true

  # Critical alerts: Telegram + Discord
  - name: "critical-alerts"
    telegram_configs:
      - bot_token: "${TELEGRAM_BOT_TOKEN}"
        chat_id: ${TELEGRAM_CHAT_ID}         
        api_url: "https://api.telegram.org"      
        parse_mode: "Markdown"                   
        send_resolved: true
        message: |
          *CRITICAL ALERT*
          {{ range .Alerts }}
          *Alert:* {{ .Labels.alertname }}
          *Service:* {{ .Labels.service }}
          *Description:* {{ .Annotations.description }}
          {{ end }}
    webhook_configs:
      - url: "${DISCORD_WEBHOOK_URL}"
        send_resolved: true

  # Warning alerts: Discord only
  - name: "warning-alerts"
    webhook_configs:
      - url: "${DISCORD_WEBHOOK_URL}"
        send_resolved: true

# Inhibition rules
inhibit_rules:

  - source_match:
      severity: "critical"
      alertname: "BackendDown"
    target_match:
      severity: "warning"
    equal: ["service"]

  - source_match:
      severity: "critical"
      alertname: "PostgresDown"
    target_match:
      severity: "warning"
      service: "postgres"
    equal: ["instance"]
