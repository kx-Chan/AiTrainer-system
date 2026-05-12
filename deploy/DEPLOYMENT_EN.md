# AiTrainer Production Deployment Guide (Cloud Server)

> This guide is based on the current `docker-compose.yml`. It walks you through deploying the project from scratch on a brand-new cloud server, including domain, HTTPS, backups, and other production-ready settings.
>
> Target environment: **Ubuntu 22.04 LTS** (for CentOS, replace `apt` with `yum/dnf`).

---

## Table of Contents

- [0. Prerequisites](#0-prerequisites)
- [1. Provision & Initialize the Server](#1-provision--initialize-the-server)
- [2. Install Docker / Docker Compose](#2-install-docker--docker-compose)
- [3. Upload Project Code](#3-upload-project-code)
- [4. Configure .env](#4-configure-env)
- [5. First Run & Verification](#5-first-run--verification)
- [6. Bind a Domain](#6-bind-a-domain)
- [7. HTTPS (Let's Encrypt)](#7-https-lets-encrypt)
- [8. Automated Database Backups](#8-automated-database-backups)
- [9. Logs & Monitoring](#9-logs--monitoring)
- [10. Ops Command Cheat Sheet](#10-ops-command-cheat-sheet)
- [11. Troubleshooting](#11-troubleshooting)
- [Post-Launch Checklist](#post-launch-checklist)

---

## 0. Prerequisites

| Item | Notes | Recommendation |
|------|------|------|
| Cloud server | Linux host | Aliyun/Tencent Cloud, **2C4G + 40G disk + 3Mbps** |
| OS image | - | Ubuntu 22.04 LTS |
| Domain (optional) | Access via domain | Aliyun Domains / NameSilo |
| AI API Key | Your LLM provider | sandboxai (as used in this project) |
| Aliyun OSS | Avatar/file storage | Used |
| SMTP account | Email verification codes | QQ/163 mail SMTP auth code |

---

## 1. Provision & Initialize the Server

### 1.1 Provision

- Aliyun ECS / Tencent CVM / Huawei Cloud: start with pay-as-you-go; switch to a yearly plan after stable.
- Choose **Ubuntu 22.04 64-bit**.
- Set a root password or upload an SSH public key.

### 1.2 Security Group (Most Important)

In the cloud console **Security Group / Firewall**, only allow:

| Port | Protocol | Purpose | Public |
|------|----------|---------|--------|
| 22 | TCP | SSH | ✅ (consider using a non-default port or IP allowlist) |
| 80 | TCP | HTTP | ✅ |
| 443 | TCP | HTTPS | ✅ |
| 3000 | TCP | Backend (container port) | ❌ (internal only) |
| 3306 | TCP | MySQL (host port) | ❌ |
| 6379 | TCP | Redis (host port) | ❌ |

### 1.3 SSH Login

```bash
ssh root@<your-public-ip>
```

### 1.4 Basic Hardening

```bash
# Update system
apt update && apt upgrade -y

# Create a non-root user (recommended; use it for daily operations)
adduser deploy
usermod -aG sudo deploy

# Common tools
apt install -y vim git curl wget htop ufw

# Enable host firewall (defense in depth)
ufw allow 22
ufw allow 80
ufw allow 443
ufw enable
```

---

## 2. Install Docker / Docker Compose

```bash
# Install Docker (official script)
curl -fsSL https://get.docker.com | bash

# Enable and start Docker
systemctl enable --now docker

# Verify
docker --version
docker compose version

# Add current user to docker group (no sudo)
usermod -aG docker $USER
newgrp docker
```

If you are on a server in mainland China, consider configuring registry mirrors:

```bash
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
EOF
systemctl restart docker
```

---

## 3. Upload Project Code

### Option A: Clone from GitHub (Recommended)

```bash
cd /opt
git clone https://github.com/kx-Chan/AiTrainer-system.git aitrainer
cd aitrainer
```

### Option B: Upload via scp from Your Local Machine

```bash
# Run on local Windows PowerShell
scp -r D:\1\AiTrainer root@<your-ip>:/opt/aitrainer
```

---

## 4. Configure .env

```bash
cd /opt/aitrainer/deploy
cp .env.example .env
vim .env
```

**Required changes** (example):

```ini
# ===== Database passwords (use strong passwords!) =====
MYSQL_ROOT_PASSWORD=replace_with_16+_random_chars
MYSQL_PASSWORD=replace_with_16+_random_chars
MYSQL_DB=ai_trainer
MYSQL_USER=aitrainer

# ===== Redis password =====
REDIS_PASSWORD=replace_with_16+_random_chars

# ===== Mail (QQ mail example) =====
MAIL_USERNAME=your_qq@qq.com
MAIL_PASSWORD=your_smtp_auth_code

# ===== Aliyun OSS =====
ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_BUCKET=your-bucket-name
ALIYUN_ACCESS_KEY_ID=LTAI...
ALIYUN_ACCESS_KEY_SECRET=...

# ===== AI / LLM =====
AI_API_KEY=sk-xxxxxx
AI_MODEL=gpt-5.4
AI_BASE_URL=https://ai.sandboxai.top/v1/

# ===== Ports (usually unchanged) =====
FRONTEND_PORT=80
BACKEND_PORT=3000
MYSQL_PORT=3306
REDIS_PORT=6379
```

Generate a strong password:

`openssl rand -base64 24`

Lock down `.env` permissions:

```bash
chmod 600 .env
```

---

## 5. First Run & Verification

```bash
cd /opt/aitrainer/deploy

# Build and start in the background
docker compose up -d --build

# Check status
docker compose ps

# Follow logs (Ctrl+C to exit, containers keep running)
docker compose logs -f backend
docker compose logs -f frontend
```

**Verify**:

- Open `http://<your-public-ip>` in a browser — you should see the frontend homepage.
- `curl http://<your-public-ip>/api/actuator/health` — should return `{"status":"UP"}`.

If the homepage works but APIs fail, see [11. Troubleshooting](#11-troubleshooting).

---

## 6. Bind a Domain

### 6.1 Buy a Domain

- Overseas: NameSilo / Cloudflare (no ICP needed)
- Mainland China: Aliyun / Tencent (ICP filing required)

### 6.2 DNS Records

Add A records in your DNS provider:

| Host | Type | Value |
|------|------|-------|
| `@` | A | server public IP |
| `www` | A | server public IP |

Wait 5–30 minutes, then verify:

```bash
ping yourdomain.com
```

### 6.3 Update nginx server_name

Edit `frontend/nginx.conf`:

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    ...
}
```

Rebuild frontend:

```bash
docker compose up -d --build frontend
```

---

## 7. HTTPS (Let's Encrypt)

This guide uses the simplest approach: apply for a certificate on the host with **certbot** and mount it into the container.

### 7.1 Request a Certificate (Stop the Container Using Port 80 First)

```bash
docker compose stop frontend

apt install -y certbot

certbot certonly --standalone \
  -d yourdomain.com \
  -d www.yourdomain.com \
  --email your@email.com \
  --agree-tos --no-eff-email

# Certificates will be located at:
# /etc/letsencrypt/live/yourdomain.com/fullchain.pem
# /etc/letsencrypt/live/yourdomain.com/privkey.pem
```

### 7.2 Update nginx.conf for HTTPS

Replace `frontend/nginx.conf` with:

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    ssl_certificate     /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_session_cache shared:SSL:10m;

    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/javascript application/json;

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location /api/ {
        proxy_pass http://backend:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    location /swagger-ui/ { proxy_pass http://backend:3000; }
    location /v3/api-docs { proxy_pass http://backend:3000; }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 7.3 Expose 443 and Mount Certificates in docker-compose.yml

In `deploy/docker-compose.yml`, update the `frontend` service:

```yaml
  frontend:
    build:
      context: ../frontend
      dockerfile: ../deploy/Dockerfile.frontend
    container_name: aitrainer-frontend
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /etc/letsencrypt/live/yourdomain.com/fullchain.pem:/etc/nginx/ssl/fullchain.pem:ro
      - /etc/letsencrypt/live/yourdomain.com/privkey.pem:/etc/nginx/ssl/privkey.pem:ro
    depends_on:
      - backend
    networks:
      - aitrainer-net
```

### 7.4 Restart

```bash
docker compose up -d --build frontend
```

Open `https://yourdomain.com`. If the browser shows a valid certificate, you are done.

### 7.5 Auto-Renew (Certificates expire every 90 days)

```bash
cat > /opt/renew-cert.sh <<'EOF'
#!/bin/bash
cd /opt/aitrainer/deploy
docker compose stop frontend
certbot renew --quiet
docker compose up -d frontend
EOF
chmod +x /opt/renew-cert.sh

(crontab -l 2>/dev/null; echo "0 3 1 * * /opt/renew-cert.sh >> /var/log/cert-renew.log 2>&1") | crontab -
```

---

## 8. Automated Database Backups

### 8.1 Create Backup Script

```bash
mkdir -p /opt/backups
cat > /opt/backup-mysql.sh <<'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/opt/backups
KEEP_DAYS=14

source /opt/aitrainer/deploy/.env

docker exec aitrainer-mysql sh -c \
  "exec mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} --all-databases --single-transaction" \
  | gzip > ${BACKUP_DIR}/mysql_${DATE}.sql.gz

find ${BACKUP_DIR} -name "mysql_*.sql.gz" -mtime +${KEEP_DAYS} -delete

echo "[$(date)] Backup OK: mysql_${DATE}.sql.gz"
EOF
chmod +x /opt/backup-mysql.sh
```

### 8.2 Cron (Run at 02:00 Daily)

```bash
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/backup-mysql.sh >> /var/log/mysql-backup.log 2>&1") | crontab -
```

### 8.3 (Optional) Sync to Aliyun OSS

Install `ossutil`, then add:

```bash
ossutil cp ${BACKUP_DIR}/mysql_${DATE}.sql.gz oss://your-bucket/backups/
```

### 8.4 Restore

```bash
gunzip < /opt/backups/mysql_20260507_020000.sql.gz | \
  docker exec -i aitrainer-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD}
```

---

## 9. Logs & Monitoring

### 9.1 Limit Docker Log Size

Edit `/etc/docker/daemon.json`:

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
```

Restart Docker:

`systemctl restart docker`

### 9.2 JVM Memory Limits (Backend)

In `docker-compose.yml` under `backend`:

```yaml
    environment:
      JAVA_OPTS: "-Xmx1g -Xms512m -XX:+UseG1GC"
    deploy:
      resources:
        limits:
          memory: 1.5G
```

### 9.3 Live Monitoring

```bash
docker stats
htop
df -h
du -sh /var/lib/docker/volumes/*
```

---

## 10. Ops Command Cheat Sheet

```bash
cd /opt/aitrainer/deploy

docker compose ps
docker compose logs --tail=200 -f backend
docker compose restart backend

git pull
docker compose up -d --build

docker exec -it aitrainer-backend sh
docker exec -it aitrainer-mysql mysql -uroot -p

docker compose down
docker compose down -v
```

---

## 11. Troubleshooting

### Q1: Frontend works, but all APIs return 502 / 404

- Check if backend failed to start: `docker compose logs backend`
- Most often: wrong secrets / DB passwords in `.env`
- Check `docker compose ps`: backend should be `healthy`

### Q2: MySQL container fails with "Access denied"

- If you changed passwords after the first run, you must recreate the volume:

  ```bash
  docker compose down
  docker volume rm deploy_mysql_data
  docker compose up -d
  ```

### Q3: CORS error after login

- Check CORS config in `backend/src/main/java/com/aitrainer/config/SecurityConfig.java`
- Add your domain (including `https://`) to `allowedOrigins`

### Q4: Out-of-memory (backend killed / OOM)

- Upgrade to 4GB RAM, or enable swap:

  ```bash
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile && swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
  ```

### Q5: HTTPS certificate request fails

- Ensure DNS points to the current server (`ping` the domain)
- Ensure port 80 is not occupied (`lsof -i:80`)
- Ensure the security group allows port 80

---

## Post-Launch Checklist

- [ ] Only expose 22/80/443 in the security group
- [ ] Root password changed / root SSH login disabled
- [ ] All `.env` passwords updated and protected with `chmod 600`
- [ ] DNS resolved and HTTP is reachable
- [ ] HTTPS enabled and auto-renew cron configured
- [ ] MySQL backup cron configured and restore tested
- [ ] Docker log size limits configured
- [ ] Backend JVM memory limits configured
- [ ] Full flow test: sign up → login → upload avatar → AI chat
- [ ] Monitoring/alerts (optional: cloud monitoring / UptimeRobot)

---
