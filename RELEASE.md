# Release v1.4.1 — Comandos

## 1. Commit y push a GitHub

```bash
git add -A
git commit -m "v1.4.1: EPG, TV, widget, categorias, backup, traducciones, admin panel, perfiles, calidad adaptativa, refresh IPTV, channel preview"
git tag v1.4.1
git push origin main --tags
```

> GitHub Actions compila firma y publica el APK automáticamente.

## 2. Desplegar backend en VPS

### 2.1 Subir main.go actualizado

```bash
# Desde tu máquina LOCAL (PowerShell):
scp -i tu-clave.pem backend-go/main.go root@<IP_DEL_VPS>:/home/ubuntu/tvxargtec-backend/
```

### 2.2 Conectarte al VPS y verificar cómo está corriendo

```bash
ssh -i tu-clave.pem root@<IP_DEL_VPS>
cd /home/ubuntu/tvxargtec-backend
```

Ya conectado, primero averiguá cómo se ejecuta el backend:

```bash
# Opción A: Buscar proceso de Go corriendo
ps aux | grep -E "main|tvxargtec|go" | grep -v grep

# Opción B: Ver servicios existentes
ls /etc/systemd/system/ | grep -E "tvxargtec|api|backend"

# Opción C: Ver si usa screen/tmux
screen -ls
tmux ls

# Opción D: Ver si es un proceso directo
ps aux --forest | head -30
```

### 2.3 Según lo que encuentres

**Caso 1 — Service systemd (recomendado):**
```bash
# Si encontraste un .service, usalo:
sudo systemctl status <NOMBRE_DEL_SERVICE>
sudo systemctl stop <NOMBRE_DEL_SERVICE>
go build -o tvxargtec-api main.go
sudo cp tvxargtec-api /usr/local/bin/tvxargtec-api
sudo systemctl start <NOMBRE_DEL_SERVICE>
```

**Caso 2 — Screen/Tmux:**
```bash
# Reconnect a la sesión
screen -r
# o
tmux attach

# Dentro: Ctrl+C para detener, luego:
go build -o tvxargtec-api main.go
./tvxargtec-api
# Ctrl+A+D (screen) o Ctrl+B+D (tmux) para desconectar
```

**Caso 3 — Proceso directo (nohup):**
```bash
# Matar proceso anterior
pkill -f "tvxargtec-api" || pkill -f "main.go"

# Compilar y ejecutar
go build -o tvxargtec-api main.go
nohup ./tvxargtec-api > api.log 2>&1 &

# Verificar
tail -f api.log
```

**Caso 4 — Crear service desde cero:**
```bash
# Compilar
go build -o tvxargtec-api main.go

# Crear service
cat > /etc/systemd/system/tvxargtec-api.service << 'EOF'
[Unit]
Description=Tvxargtec API
After=network.target

[Service]
ExecStart=/home/ubuntu/tvxargtec-backend/tvxargtec-api
WorkingDirectory=/home/ubuntu/tvxargtec-backend
Environment=DSN=root:tvxargtec2025@tcp(localhost:3306)/tvxargtec?parseTime=true
Restart=always
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable tvxargtec-api
systemctl start tvxargtec-api
systemctl status tvxargtec-api
```

### 2.4 Verificar

```bash
curl -s https://apitvxargtec.duckdns.org/api/health
# Debería responder: {"code":200,"message":"ok"}
```
