#!/bin/bash

# Script de configuración para VPS AWS (Ubuntu 22.04/24.04)
# TVXargtec Online - Backend Setup con MySQL

echo "🚀 Iniciando configuración del servidor TVXargtec..."

# 1. Actualizar el sistema
sudo apt update && sudo apt upgrade -y

# 2. Configurar SWAP (2GB) para compensar el 1GB de RAM
echo "🛠 Configurando memoria SWAP..."
if [ ! -f /swapfile ]; then
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

# 3. Instalar dependencias básicas (Incluyendo MySQL y Go)
echo "📦 Instalando dependencias (Nginx, MySQL, Go)..."
sudo apt install -y nginx git curl ufw certbot python3-certbot-nginx mysql-server golang-go

# 4. Configurar Firewall (UFW)
echo "🛡 Configurando Firewall..."
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw --force enable

# 5. Iniciar y asegurar MySQL
echo "🗄 Configurando MySQL..."
sudo systemctl start mysql
sudo systemctl enable mysql
# Nota: Deberás ejecutar 'sudo mysql_secure_installation' manualmente para configurar la contraseña root.

# 6. Crear estructura de directorios para el proyecto
mkdir -p ~/tvxargtec-backend
mkdir -p ~/tvxargtec-website

# 7. Configurar Nginx
echo "🌐 Configurando Nginx..."
cat <<EOF | sudo tee /etc/nginx/sites-available/tvxargtec
server {
    listen 80;
    server_name apitvxargtec.duckdns.org;

    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }

    location / {
        root /home/ubuntu/tvxargtec-website;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }
}
EOF

if [ ! -f /etc/nginx/sites-enabled/tvxargtec ]; then
    sudo ln -s /etc/nginx/sites-available/tvxargtec /etc/nginx/sites-enabled/
fi
sudo rm -f /etc/nginx/sites-enabled/default
sudo systemctl restart nginx

echo "✅ ¡Configuración completada!"
echo "--------------------------------------------------"
echo "Próximos pasos:"
echo "1. Ejecuta 'sudo mysql' y crea la base de datos 'tvxargtec_db'"
echo "2. Sube tu código Go a ~/tvxargtec-backend"
echo "3. Ejecuta 'go run main.go' en el puerto 8080"
echo "--------------------------------------------------"
