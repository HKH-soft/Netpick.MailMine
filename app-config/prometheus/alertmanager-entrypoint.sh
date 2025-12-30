#!/bin/sh
set -e

TEMPLATE=/etc/alertmanager/alertmanager.yml.tpl
OUT=/etc/alertmanager/alertmanager.yml

# Render template to actual config
if [ -f "$TEMPLATE" ]; then
  echo "[alertmanager] Rendering template $TEMPLATE -> $OUT"

  if command -v envsubst >/dev/null 2>&1; then
    envsubst < "$TEMPLATE" > "$OUT"
  else
    echo "[alertmanager] envsubst not found, installing gettext-base..."
    apt-get update -y && apt-get install -y --no-install-recommends gettext-base >/dev/null
    envsubst < "$TEMPLATE" > "$OUT"
  fi
  echo "[alertmanager] Rendered config:" && sed -n '1,120p' "$OUT"
fi

# Execute Alertmanager with generated config
exec /bin/alertmanager --config.file="$OUT" --storage.path=/alertmanager
