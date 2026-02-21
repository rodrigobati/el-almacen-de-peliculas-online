#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose-full.yml"

echo "==> Reset completo: containers + redes + volúmenes"
docker compose -f "${COMPOSE_FILE}" down -v --remove-orphans

echo "==> Volúmenes eliminados. Ejecutá scripts/up.sh para reconstruir."
