#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose-full.yml"

echo "==> Bajando stack (sin borrar volúmenes)..."
docker compose -f "${COMPOSE_FILE}" down --remove-orphans
