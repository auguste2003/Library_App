#!/bin/bash
# Manuelles Deployment Script (Fallback für GitHub Actions)

echo "🚀 Starte manuelles Deployment..."

cd /opt/library-app

echo "📥 Hole neueste Änderungen von GitHub..."
git pull origin main

cd production

echo "🐳 Lade neueste Docker Images..."
# Setze Umgebungsvariablen aus .env.production für den Pull (falls nötig)
export $(grep -v '^#' .env.production | xargs)
docker compose -f docker-compose.prod.yml pull

echo "🔄 Starte Container neu..."
docker compose -f docker-compose.prod.yml up -d

echo "🧹 Bereinige alte Images..."
docker system prune -f

echo "✅ Deployment abgeschlossen!"
docker compose -f docker-compose.prod.yml ps
