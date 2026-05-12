# AiTrainer Docker Deployment Guide

## Directory Structure

```text
deploy/
├── .env.example              # Environment variable template
├── docker-compose.yml        # Docker Compose configuration
├── Dockerfile.backend        # Backend image build file
├── Dockerfile.frontend       # Frontend image build file
├── deploy.sh                 # Linux/Mac deployment script
├── deploy.bat                # Windows deployment script
└── mysql/
    └── init/
        └── init.sql          # Database initialization script
```

## Quick Start

### 1. Prerequisites

Make sure you have installed:

- Docker 20.10+
- Docker Compose 2.0+ (or use `docker compose`)

### 2. Configure Environment Variables

```bash
cd deploy
cp .env.example .env
# Edit .env and fill in real values
# To avoid conflicts with an existing local MySQL/Redis, this project uses 3307 (instead of 3306) for MySQL
# and 6380 (instead of 6379) for Redis by default. You can adjust ports as needed in .env.
```

### 3. One-Click Deployment

**Windows:**

```cmd
deploy.bat start
```

**Linux/Mac:**

```bash
chmod +x deploy.sh
./deploy.sh start
```

### 4. Verify

After a successful deployment, visit:

- Frontend: <http://localhost:80>
- API docs (Swagger UI): <http://localhost/swagger-ui/index.html>

---

## Common Commands

| Command | Description |
|------|------|
| `./deploy.sh start` | Full deployment (pull + build + start) |
| `./deploy.sh dev` | Start in dev mode (reuse existing images) |
| `./deploy.sh stop` | Stop services |
| `./deploy.sh restart` | Restart services |
| `./deploy.sh logs` | View logs |
| `./deploy.sh status` | View service status |
| `./deploy.sh build` | Build images only |
| `./deploy.sh clean` | Clean containers and volumes |

---

## Architecture Overview

```text
                    ┌─────────────────┐
                    │     Browser     │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   Nginx (FE)    │  :80
                    │  / → static     │
                    │  /api/* → BE    │
                    └────────┬────────┘
                             │
              ┌─────────────┴─────────────┐
              │                           │
     ┌────────▼────────┐        ┌────────▼────────┐
     │  Spring Boot     │        │   MySQL 8.0     │
     │  (BE :3000)      │        │    (:3307)      │
     └────────┬────────┘        └─────────────────┘
              │
     ┌────────▼────────┐
     │    Redis 7       │
     │    (:6380)       │
     └──────────────────┘
```

### Containers

| Container | Image | Port | Notes |
|--------|------|------|------|
| aitrainer-frontend | nginx:alpine | 80 | Static assets + API reverse proxy |
| aitrainer-backend | maven:3.9-eclipse-temurin-17 | 3000 | Spring Boot backend |
| aitrainer-mysql | mysql:8.0 | 3307 | MySQL database |
| aitrainer-redis | redis:7-alpine | 6380 | Redis cache |

---

## Nginx Routing

The frontend Nginx config (`frontend/nginx.conf`) implements the following routing rules:

### Routes

| Path | Target | Notes |
|------|---------|------|
| `/` | static files | Vue frontend |
| `/api/*` | backend:3000 | API reverse proxy |
| `/swagger-ui/*` | backend:3000 | API docs |
| `/v3/api-docs` | backend:3000 | OpenAPI spec |

### Key Features

1. Gzip compression
2. Long-term caching for static assets (JS/CSS/images)
3. Header forwarding (e.g., X-Real-IP)
4. Timeout defaults (60s) to avoid long-request blocking
5. SPA routing support (Vue Router history mode)

---

## Environment Variables

### Database

```env
MYSQL_ROOT_PASSWORD=AiTrainer_Admin_2026
MYSQL_DB=ai_trainer
MYSQL_USER=aitrainer_user
MYSQL_PASSWORD=AiTrainer_Pass_2026
MYSQL_PORT=3307
```

### Redis

```env
REDIS_PORT=6380
REDIS_PASSWORD=AiTrainer_Redis_2026
```

### Service Ports

```env
BACKEND_PORT=3000
FRONTEND_PORT=80
```

### Email (QQ Mail example)

```env
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_smtp_auth_code
```

### Aliyun OSS

```env
ALIYUN_OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_BUCKET=your-bucket-name
ALIYUN_ACCESS_KEY_ID=your-access-key-id
ALIYUN_ACCESS_KEY_SECRET=your-access-key-secret
```

### AI Model

```env
AI_API_KEY=your-openai-api-key
AI_MODEL=gpt-5.4
AI_BASE_URL=https://api.openai.com/v1/
```

---

## Troubleshooting

### View Logs

```bash
docker compose logs -f
```

### Check Status

```bash
docker compose ps
```

### Recreate a Service

```bash
docker compose up -d --force-recreate <service>
```

### Enter a Container

```bash
docker exec -it aitrainer-backend /bin/sh
```

### Clean and Re-Deploy

```bash
docker compose down -v
./deploy.sh start
```

---

## Health Checks

Each service includes health checks:

- Backend: `curl http://localhost:3000/actuator/health`
- Frontend: `wget http://localhost/`
- MySQL: `mysqladmin ping`
- Redis: `redis-cli ping`

---

## Production Notes

1. Change all default passwords in `.env`
2. Configure HTTPS (SSL certificates) via Nginx
3. Restrict exposed ports using firewall rules
4. Configure log rotation to prevent disk exhaustion
5. Back up MySQL and Redis regularly

### HTTPS Example

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # The rest is the same as the HTTP server block
}
```
