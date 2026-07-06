# Despliegue de main.go en VPS (AWS Ubuntu)

## 1. Conectar al VPS

```bash
ssh -i tu-clave.pem ubuntu@<IP_DEL_VPS>
```

## 2. Ir al directorio del backend

```bash
cd /home/ubuntu/tvxargtec-backend
```

O si es primera vez:

```bash
mkdir -p /home/ubuntu/tvxargtec-backend
```

## 3. Subir el archivo main.go actualizado

Desde tu máquina local (PowerShell):

```powershell
scp -i tu-clave.pem backend-go/main.go ubuntu@<IP_DEL_VPS>:/home/ubuntu/tvxargtec-backend/main.go
```

## 4. Compilar la nueva versión

```bash
cd /home/ubuntu/tvxargtec-backend
go build -o tvxargtec-api main.go
```

## 5. Detener el servicio actual

```bash
sudo systemctl stop tvxargtec-api
```

## 6. Copiar el binario

```bash
sudo cp tvxargtec-api /usr/local/bin/tvxargtec-api
```

## 7. Verificar que el service account JSON existe

```bash
ls -la /etc/tvxargtec/tv-x-arg-tec-firebase-adminsdk-fbsvc-52ae02a69a.json
```

Si no existe, súbelo:

```powershell
scp -i tu-clave.pem backend-go/tv-x-arg-tec-firebase-adminsdk-fbsvc-52ae02a69a.json ubuntu@<IP_DEL_VPS>:/home/ubuntu/tvxargtec-backend/
ssh -i tu-clave.pem ubuntu@<IP_DEL_VPS> "sudo mkdir -p /etc/tvxargtec && sudo cp /home/ubuntu/tvxargtec-backend/tv-x-arg-tec-firebase-adminsdk-fbsvc-52ae02a69a.json /etc/tvxargtec/"
```

## 8. Crear/verificar service systemd

```bash
sudo tee /etc/systemd/system/tvxargtec-api.service << 'EOF'
[Unit]
Description=Tvxargtec API Go Backend
After=network.target

[Service]
ExecStart=/usr/local/bin/tvxargtec-api
WorkingDirectory=/home/ubuntu/tvxargtec-backend
Environment=DSN=root:tvxargtec2025@tcp(localhost:3306)/tvxargtec?parseTime=true
Environment=FCM_CREDENTIALS=/etc/tvxargtec/tv-x-arg-tec-firebase-adminsdk-fbsvc-52ae02a69a.json
User=ubuntu
Group=ubuntu
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
```

## 9. Recargar e iniciar servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable tvxargtec-api
sudo systemctl start tvxargtec-api
```

## 10. Verificar que funciona

```bash
sudo systemctl status tvxargtec-api
journalctl -u tvxargtec-api -n 20 --no-pager
```

## 11. Probar endpoint de backup

```bash
curl -s https://apitvxargtec.duckdns.org/api/health
```

## Comandos rápidos (después de la primera vez)

```bash
# Subir nuevo main.go
scp -i tu-clave.pem backend-go/main.go ubuntu@<IP>:/home/ubuntu/tvxargtec-backend/

# Compilar y reiniciar en el VPS
ssh -i tu-clave.pem ubuntu@<IP> "cd /home/ubuntu/tvxargtec-backend && go build -o tvxargtec-api main.go && sudo systemctl stop tvxargtec-api && sudo cp tvxargtec-api /usr/local/bin/tvxargtec-api && sudo systemctl start tvxargtec-api && sudo systemctl status tvxargtec-api"
```
