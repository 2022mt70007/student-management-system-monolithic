#!/usr/bin/env bash
# Deploy monolith to VM (veritascampus.page). Run from repo root on the server.
#
# One-time VM setup:
#   cd /opt
#   git clone https://github.com/2022mt70007/student-management-system-monolithic.git sms-monolith
#   cd sms-monolith && git checkout feature/firstBrach
#   cp .env.production.example .env && nano .env
#   chmod +x scripts/deploy-monolith-vm.sh && ./scripts/deploy-monolith-vm.sh
#
# After you push changes from your PC:
#   cd /opt/sms-monolith && ./scripts/deploy-monolith-vm.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -d .git ]]; then
  echo "==> Pulling latest code from git..."
  git fetch origin
  git pull --ff-only origin "$(git rev-parse --abbrev-ref HEAD)"
else
  echo "Warning: not a git repo — deploy uses files on disk only."
  echo "Clone from GitHub: git clone https://github.com/2022mt70007/student-management-system-monolithic.git"
fi

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.production.example to .env and fill in values."
  exit 1
fi

DOCKER="docker"
if ! docker info >/dev/null 2>&1; then
  DOCKER="sudo docker"
fi

echo "==> Building and starting backend (docker compose prod)..."
$DOCKER compose -f docker-compose.prod.yml --env-file .env up -d --build

echo "==> Waiting for monolith to start..."
sleep 8
if curl -sf http://127.0.0.1:8090/actuator/health >/dev/null 2>&1 || curl -sf http://127.0.0.1:8090/swagger-ui.html >/dev/null 2>&1; then
  echo "    Backend is responding on 127.0.0.1:8090"
else
  echo "    Warning: backend may still be starting. Check: docker logs sms-monolith --tail 50"
fi

echo "==> Building frontend (same-origin /api, no VITE_API_BASE_URL)..."
cd frontend
npm ci
npm run build

echo "==> Publishing frontend to /var/www/veritascampus ..."
sudo mkdir -p /var/www/veritascampus
sudo rsync -a --delete dist/ /var/www/veritascampus/

echo "==> Installing nginx site config..."
sudo cp "$ROOT/docs/nginx-veritascampus.conf" /etc/nginx/sites-available/veritascampus
sudo ln -sf /etc/nginx/sites-available/veritascampus /etc/nginx/sites-enabled/veritascampus
sudo nginx -t
sudo systemctl reload nginx

echo ""
echo "Done."
echo "Logs: $DOCKER logs sms-monolith -f"
