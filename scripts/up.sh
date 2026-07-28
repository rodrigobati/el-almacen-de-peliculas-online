#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose-full.yml"

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker no está instalado o no está en PATH." >&2
  exit 1
fi

echo "==> Levantando stack completo..."
docker compose -f "${COMPOSE_FILE}" up -d --build

echo
echo "==> Estado actual"
docker compose -f "${COMPOSE_FILE}" ps

echo
echo "==> URLs"
echo "- Frontend:      http://localhost:5173"
echo "- API Gateway:   http://localhost:9500"
echo "- Catálogo API:  http://localhost:8081"
echo "- Ventas API:    http://localhost:8083"
echo "- Keycloak:      http://localhost:9090"
echo "- RabbitMQ UI:   http://localhost:15672"

echo
echo "==> Credenciales de desarrollo (placeholder)"
echo "- Keycloak admin: admin / admin"
echo "- RabbitMQ:       guest / guest"
echo "- MySQL catálogo: almacen / almacen (host 3307)"
