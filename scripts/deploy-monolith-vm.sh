#!/usr/bin/env bash
# Deploy monolith to VM (veritascampus.page). Run from repo root on the server.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.production.example to .env and fill in values."
  exit 1
fi

echo "==> Building and starting backend (docker compose prod)..."
docker compose -f docker-compose.prod.yml --env-file .env up -d --build

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
echo "Done. Next steps:"
echo "  1. Ensure DNS A records for veritascampus.page -> this VM IP"
echo "  2. HTTPS: sudo certbot --nginx -d veritascampus.page -d www.veritascampus.page"
echo "  3. Open https://veritascampus.page/register and complete admin registration"
echo ""
echo "Logs: docker logs sms-monolith -f"
