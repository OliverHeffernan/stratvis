#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
npm run -w contracts generate:types --prefix "$ROOT_DIR"
docker compose -f "$ROOT_DIR/backend/docker-compose.yml" up --build -d
npm run -w frontend build --prefix "$ROOT_DIR"
