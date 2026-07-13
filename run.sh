#!/usr/bin/env bash
#
# run.sh — Run the Backend with Maven Wrapper and the chosen Spring profile.
#
# Usage:
#   ./run.sh          # defaults to 'dev'
#   ./run.sh dev      # SQLite, no Redis
#   ./run.sh pro      # PostgreSQL + Redis (production defaults)
#   ./run.sh test     # H2 in-memory
#
# Extra args after the profile are forwarded to Spring Boot, e.g.:
#   ./run.sh dev --server.port=9090

set -euo pipefail

PROFILE="${1:-dev}"
shift 2>/dev/null || true

# Resolve to project root (where this script lives)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${SCRIPT_DIR}/Backend"

# Validate profile
case "${PROFILE}" in
  dev|pro|staging|test) ;;
  *)
    echo "ERROR: Unknown profile '${PROFILE}'. Choose from: dev, pro, staging, test"
    exit 1
    ;;
esac

# Ensure mvnw is executable
chmod +x "${BACKEND_DIR}/mvnw" 2>/dev/null || true

echo "=== Starting MailMine Backend [profile: ${PROFILE}] ==="

cd "${BACKEND_DIR}"
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles="${PROFILE}" \
  "$@"
