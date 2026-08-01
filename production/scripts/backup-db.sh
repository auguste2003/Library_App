#!/bin/bash

# Backup Konfiguration
BACKUP_DIR="/opt/library-app/backups"
DATE=$(date +"%Y-%m-%d_%H-%M-%S")
COMPOSE_FILE="/opt/library-app/production/docker-compose.prod.yml"

# Erstelle Backup-Verzeichnis
mkdir -p "$BACKUP_DIR/$DATE"

echo "Starte Datenbank-Backup ($DATE)..."

# Prüfe ob Compose läuft
if ! docker compose -f "$COMPOSE_FILE" ps | grep -q "Up"; then
    echo "Fehler: Docker Compose Services laufen nicht!"
    exit 1
fi

# Auth DB Backup
echo "Sichere auth_db..."
docker compose -f "$COMPOSE_FILE" exec -T auth-db pg_dump -U library_admin auth_db | gzip > "$BACKUP_DIR/$DATE/auth-db.sql.gz"

# Library DB Backup
echo "Sichere library_db..."
docker compose -f "$COMPOSE_FILE" exec -T library-db pg_dump -U library_admin library_db | gzip > "$BACKUP_DIR/$DATE/library-db.sql.gz"

echo "✅ Backup abgeschlossen in $BACKUP_DIR/$DATE"

# Alte Backups löschen (älter als 7 Tage)
echo "🧹 Suche nach alten Backups zum Löschen..."
find "$BACKUP_DIR" -maxdepth 1 -type d -mtime +7 -name "20*" -exec rm -rf {} +
echo "✅ Alte Backups bereinigt."
