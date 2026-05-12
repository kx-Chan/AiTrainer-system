# AiTrainer 云服务器上线操作手册

> 本手册基于当前项目的 `docker-compose.yml` 编写，从零开始把项目部署到一台全新的云服务器上，并完成域名、HTTPS、备份等生产化配置。
>
> 适用环境：**Ubuntu 22.04 LTS**（CentOS 用户把 `apt` 换成 `yum/dnf` 即可）

---

## 目录

- [0. 准备清单](#0-准备清单)
- [1. 购买与初始化云服务器](#1-购买与初始化云服务器)
- [2. 安装 Docker / Docker Compose](#2-安装-docker--docker-compose)
- [3. 上传项目代码](#3-上传项目代码)
- [4. 配置 .env 环境变量](#4-配置-env-环境变量)
- [5. 首次启动 & 验证](#5-首次启动--验证)
- [6. 绑定域名](#6-绑定域名)
- [7. 配置 HTTPS（Let's Encrypt 免费证书）](#7-配置-https-lets-encrypt-免费证书)
- [8. 数据库自动备份](#8-数据库自动备份)
- [9. 日志与监控](#9-日志与监控)
- [10. 常用运维命令速查](#10-常用运维命令速查)
- [11. 故障排查](#11-故障排查)

---

## 0. 准备清单

| 项目 | 说明 | 推荐 |
|------|------|------|
| 云服务器 | Linux 主机 | 阿里云/腾讯云，**2核4G + 40G + 3Mbps** |
| 操作系统 | 镜像 | Ubuntu 22.04 LTS |
| 域名（可选） | 用来访问网站 | 阿里云万网 / Namesilo |
| AI API Key | 你接的大模型 | 项目里用的 sandboxai |
| 阿里云 OSS | 头像存储 | 已用 |
| SMTP 邮箱 | 验证码发送 | QQ/163 邮箱授权码 |

---

## 1. 购买与初始化云服务器

### 1.1 购买
- 阿里云 ECS / 腾讯云 CVM / 华为云，按量计费先试用，稳定后转包年。
- 系统选 **Ubuntu 22.04 64位**。
- 设置 root 密码 或 上传 SSH 公钥。

### 1.2 安全组（最重要！）
在云控制台 **安全组 / 防火墙** 中只放行以下端口：

| 端口 | 协议 | 用途 | 是否对公网开放 |
|------|------|------|---------------|
| 22 | TCP | SSH | ✅（建议改非默认端口或限制 IP） |
| 80 | TCP | HTTP | ✅ |
| 443 | TCP | HTTPS | ✅ |
| 3000 | TCP | Backend（容器内端口） | ❌ 不开放（只走容器内网） |
| 3306 | TCP | MySQL（宿主机端口，本项目用） | ❌ 不开放 |
| 6379 | TCP | Redis（宿主机端口，本项目用） | ❌ 不开放 |

### 1.3 SSH 登录
```bash
ssh root@你的服务器公网IP
```

### 1.4 基础加固
```bash
# 更新系统
apt update && apt upgrade -y

# 创建非 root 用户（推荐，后续都用这个用户操作）
adduser deploy
usermod -aG sudo deploy

# 安装常用工具
apt install -y vim git curl wget htop ufw

# 启用本机防火墙（双保险）
ufw allow 22
ufw allow 80
ufw allow 443
ufw enable
```

---

## 2. 安装 Docker / Docker Compose

```bash
# 一键安装 Docker（官方脚本）
curl -fsSL https://get.docker.com | bash

# 启动并设为开机自启
systemctl enable --now docker

# 验证
docker --version
docker compose version   # Docker 20+ 已内置 compose 子命令

# 把当前用户加入 docker 组（免 sudo）
usermod -aG docker $USER
newgrp docker
```

如果在国内服务器，建议配置镜像加速：
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

## 3. 上传项目代码

### 方式 A：从 GitHub 拉取（推荐）
```bash
cd /opt
git clone https://github.com/kx-Chan/AiTrainer-system.git aitrainer
cd aitrainer
```

### 方式 B：本地 scp 上传
```bash
# 在本地 Windows PowerShell 执行
scp -r D:\1\AiTrainer root@你的服务器IP:/opt/aitrainer
```

---

## 4. 配置 .env 环境变量

```bash
cd /opt/aitrainer/deploy
cp .env.example .env
vim .env
```

**必须修改的项**（示例）：
```ini
# ===== 数据库密码（务必使用强密码！）=====
MYSQL_ROOT_PASSWORD=请换成16位以上随机串
MYSQL_PASSWORD=请换成16位以上随机串
MYSQL_DB=ai_trainer
MYSQL_USER=aitrainer

# ===== Redis 密码 =====
REDIS_PASSWORD=请换成16位以上随机串

# ===== 邮件服务（QQ邮箱示例）=====
MAIL_USERNAME=your_qq@qq.com
MAIL_PASSWORD=你的SMTP授权码

# ===== 阿里云 OSS =====
ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_BUCKET=你的bucket名
ALIYUN_ACCESS_KEY_ID=LTAI...
ALIYUN_ACCESS_KEY_SECRET=...

# ===== AI 大模型 =====
AI_API_KEY=sk-xxxxxx
AI_MODEL=gpt-5.4
AI_BASE_URL=https://ai.sandboxai.top/v1/

# ===== 端口（一般不改）=====
FRONTEND_PORT=80
BACKEND_PORT=3000
MYSQL_PORT=3306
REDIS_PORT=6379
```

> 💡 生成强密码命令：`openssl rand -base64 24`

设置文件权限，防止泄露：
```bash
chmod 600 .env
```

---

## 5. 首次启动 & 验证

```bash
cd /opt/aitrainer/deploy

# 构建镜像并后台启动（首次构建较慢，5-15 分钟）
docker compose up -d --build

# 查看启动状态
docker compose ps

# 实时查看日志（Ctrl+C 退出，不影响运行）
docker compose logs -f backend
docker compose logs -f frontend
```

**验证**：
- 浏览器打开 `http://你的服务器公网IP` —— 应能看到前端首页
- `curl http://你的服务器公网IP/api/actuator/health` —— 应返回 `{"status":"UP"}`

如果首页能打开但 API 报错，参考 [11. 故障排查](#11-故障排查)。

---

## 6. 绑定域名

### 6.1 购买域名
- 国外：Namesilo / Cloudflare（无需备案）
- 国内：阿里云 / 腾讯云（**必须 ICP 备案**，约 20 天）

### 6.2 DNS 解析
在域名服务商控制台添加 A 记录：

| 主机记录 | 类型 | 值 |
|---------|------|-----|
| `@` | A | 服务器公网IP |
| `www` | A | 服务器公网IP |

等 5-30 分钟生效，验证：
```bash
ping yourdomain.com
```

### 6.3 修改 nginx server_name
编辑 `frontend/nginx.conf`：
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;   # ← 改这里
    ...
}
```

重新构建前端：
```bash
docker compose up -d --build frontend
```

---

## 7. 配置 HTTPS（Let's Encrypt 免费证书）

推荐使用 **certbot + 宿主机 nginx** 反代方案，或者直接用 **acme.sh** 配合容器。这里给最简单的方案：**在宿主机用 certbot 申请证书 → 挂载到容器**。

### 7.1 申请证书（先停掉占用 80 端口的容器）
```bash
# 临时停前端释放 80 端口
docker compose stop frontend

# 安装 certbot
apt install -y certbot

# 申请证书（standalone 模式，会临时启 80 端口）
certbot certonly --standalone \
  -d yourdomain.com \
  -d www.yourdomain.com \
  --email your@email.com \
  --agree-tos --no-eff-email

# 证书会生成在：
# /etc/letsencrypt/live/yourdomain.com/fullchain.pem
# /etc/letsencrypt/live/yourdomain.com/privkey.pem
```

### 7.2 修改 nginx.conf 加入 HTTPS
编辑 `frontend/nginx.conf`，**整体替换**为：

```nginx
# HTTP -> HTTPS 强制跳转
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$host$request_uri;
}

# HTTPS 主站
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

### 7.3 修改 docker-compose.yml 暴露 443 + 挂载证书
编辑 `deploy/docker-compose.yml` 中的 frontend 服务：

```yaml
  frontend:
    build:
      context: ../frontend
      dockerfile: ../deploy/Dockerfile.frontend
    container_name: aitrainer-frontend
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"           # ← 新增
    volumes:                # ← 新增
      - /etc/letsencrypt/live/yourdomain.com/fullchain.pem:/etc/nginx/ssl/fullchain.pem:ro
      - /etc/letsencrypt/live/yourdomain.com/privkey.pem:/etc/nginx/ssl/privkey.pem:ro
    depends_on:
      - backend
    networks:
      - aitrainer-net
```

### 7.4 重启
```bash
docker compose up -d --build frontend
```

打开 `https://yourdomain.com` 看到小绿锁就成功了。

### 7.5 证书自动续期（90 天到期）
```bash
# 写一个脚本：先停容器→续期→重启容器
cat > /opt/renew-cert.sh <<'EOF'
#!/bin/bash
cd /opt/aitrainer/deploy
docker compose stop frontend
certbot renew --quiet
docker compose up -d frontend
EOF
chmod +x /opt/renew-cert.sh

# 加到 crontab，每月 1 号凌晨 3 点执行
(crontab -l 2>/dev/null; echo "0 3 1 * * /opt/renew-cert.sh >> /var/log/cert-renew.log 2>&1") | crontab -
```

---

## 8. 数据库自动备份

### 8.1 创建备份脚本
```bash
mkdir -p /opt/backups
cat > /opt/backup-mysql.sh <<'EOF'
#!/bin/bash
# MySQL 自动备份脚本
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/opt/backups
KEEP_DAYS=14

# 从 .env 读取密码
source /opt/aitrainer/deploy/.env

# 导出
docker exec aitrainer-mysql sh -c \
  "exec mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} --all-databases --single-transaction" \
  | gzip > ${BACKUP_DIR}/mysql_${DATE}.sql.gz

# 清理超过 14 天的旧备份
find ${BACKUP_DIR} -name "mysql_*.sql.gz" -mtime +${KEEP_DAYS} -delete

echo "[$(date)] Backup OK: mysql_${DATE}.sql.gz"
EOF
chmod +x /opt/backup-mysql.sh
```

### 8.2 加入 crontab，每天凌晨 2 点执行
```bash
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/backup-mysql.sh >> /var/log/mysql-backup.log 2>&1") | crontab -
```

### 8.3 （可选）同步到阿里云 OSS
安装 `ossutil` → 在脚本最后加一行：
```bash
ossutil cp ${BACKUP_DIR}/mysql_${DATE}.sql.gz oss://your-bucket/backups/
```

### 8.4 恢复备份
```bash
gunzip < /opt/backups/mysql_20260507_020000.sql.gz | \
  docker exec -i aitrainer-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD}
```

---

## 9. 日志与监控

### 9.1 限制 Docker 日志大小（防止撑爆磁盘）
编辑 `/etc/docker/daemon.json`：
```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
```
重启：`systemctl restart docker`

### 9.2 给后端容器加 JVM 内存限制
在 `docker-compose.yml` 的 backend 服务下加：
```yaml
    environment:
      JAVA_OPTS: "-Xmx1g -Xms512m -XX:+UseG1GC"
    deploy:
      resources:
        limits:
          memory: 1.5G
```

### 9.3 实时监控
```bash
# 容器资源占用
docker stats

# 系统资源
htop

# 磁盘
df -h
du -sh /var/lib/docker/volumes/*
```

---

## 10. 常用运维命令速查

```bash
cd /opt/aitrainer/deploy

# 查看所有服务状态
docker compose ps

# 查看日志（最近 200 行 + 实时跟踪）
docker compose logs --tail=200 -f backend

# 重启某个服务
docker compose restart backend

# 更新代码后重新部署
git pull
docker compose up -d --build

# 进入容器排查
docker exec -it aitrainer-backend sh
docker exec -it aitrainer-mysql mysql -uroot -p

# 完全停止 & 清理（保留数据卷）
docker compose down

# ⚠️ 危险：连数据库一起删
docker compose down -v
```

---

## 11. 故障排查

### Q1：前端能打开，但 API 全部 502 / 404
- `docker compose logs backend` 看后端是不是启动失败
- 大概率是 `.env` 里的密钥/数据库密码错了
- 检查 `docker compose ps`，backend 状态应该是 `healthy`

### Q2：MySQL 容器启动失败 "Access denied"
- 第一次启动后改密码无效，需要清理数据卷重来：
  ```bash
  docker compose down
  docker volume rm deploy_mysql_data
  docker compose up -d
  ```

### Q3：前端登录后报 CORS 错误
- 检查 `backend/src/main/java/com/aitrainer/config/SecurityConfig.java` 里的 CORS 配置
- 把你的域名（含 https://）加入 `allowedOrigins`

### Q4：内存不足，backend OOM 被 kill
- 升级到 4G 内存，或开启 swap：
  ```bash
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile && swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
  ```

### Q5：HTTPS 证书申请失败
- 确认域名 DNS 已经解析到当前服务器（`ping` 一下）
- 确认 80 端口没被其他程序占用（`lsof -i:80`）
- 确认安全组放行了 80

---

## 上线后 Checklist ✅

- [ ] 服务器安全组只开 22/80/443
- [ ] root 密码已改 / 禁用 root SSH 登录
- [ ] `.env` 所有密码已改为强密码且 `chmod 600`
- [ ] 域名解析生效，HTTP 可访问
- [ ] HTTPS 配置完成，证书自动续期已加 cron
- [ ] MySQL 自动备份脚本已加 cron 并验证过恢复流程
- [ ] Docker 日志大小限制已配置
- [ ] Backend JVM 内存限制已配置
- [ ] 注册一个测试账号走通"注册→登录→上传头像→AI 对话"全流程
- [ ] 监控告警（可选：阿里云云监控/UptimeRobot）

---

