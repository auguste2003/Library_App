#!/bin/bash

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Generiere sichere Secrets für .env.production...${NC}"

# Prüfe, ob die Template-Datei existiert
if [ ! -f "../.env.production.template" ]; then
    echo "Fehler: ../.env.production.template nicht gefunden!"
    exit 1
fi

# Generiere 32-Zeichen Passwort für DB
DB_PASS=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 32)

# Generiere sicheren JWT Secret
JWT_SECRET=$(openssl rand -hex 32)

# Erstelle .env.production basierend auf Template
cat ../.env.production.template | \
sed "s/generate_strong_password_here/$DB_PASS/g" | \
sed "s/generate_256bit_secure_key_here/$JWT_SECRET/g" \
> ../.env.production

# Stelle sicher, dass die Dateirechte restriktiv sind
chmod 600 ../.env.production

echo -e "${GREEN}✅ .env.production wurde erfolgreich im Ordner 'production' erstellt!${NC}"
echo -e "${YELLOW}⚠️ WICHTIG: Bitte öffne 'production/.env.production' und ergänze manuell noch:${NC}"
echo -e "   - DOMAIN_NAME"
echo -e "   - E-Mail SMTP Daten (MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD)"
echo -e "   - GITHUB_REPOSITORY_OWNER (Dein GitHub Benutzername für die Images)"
