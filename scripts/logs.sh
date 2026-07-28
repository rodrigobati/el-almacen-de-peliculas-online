#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose-full.yml"

SERVICE="${1:-}"

if [[ -n "${SERVICE}" ]]; then
  docker compose -f "${COMPOSE_FILE}" logs -f --tail=200 "${SERVICE}"
else
  docker compose -f "${COMPOSE_FILE}" logs -f --tail=200
fi
