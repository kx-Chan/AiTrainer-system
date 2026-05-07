# AiTrainer Docker 部署指南

## 📁 目录结构

```
deploy/
├── .env.example              # 环境变量模板
├── docker-compose.yml       # Docker Compose 配置
├── Dockerfile.backend       # 后端镜像构建文件
├── Dockerfile.frontend      # 前端镜像构建文件
├── deploy.sh                # Linux/Mac 部署脚本
├── deploy.bat               # Windows 部署脚本
└── mysql/
    └── init/
        └── init.sql         # 数据库初始化脚本
```

## 🚀 快速部署

### 1. 环境准备

确保已安装以下软件：
- Docker 20.10+
- Docker Compose 2.0+ (或使用 `docker compose`)

### 2. 配置环境变量

```bash
cd deploy
cp .env.example .env
# 编辑 .env 文件填入实际配置
```

### 3. 一键部署

**Windows:**
```cmd
deploy.bat start
```

**Linux/Mac:**
```bash
chmod +x deploy.sh
./deploy.sh start
```

### 4. 验证部署

部署成功后访问：
- 前端地址: http://localhost:80
- API文档: http://localhost/swagger-ui/index.html

---

## 📋 常用命令

| 命令 | 说明 |
|------|------|
| `./deploy.sh start` | 完整部署（拉取代码 + 构建 + 启动） |
| `./deploy.sh dev` | 开发模式启动（使用现有镜像） |
| `./deploy.sh stop` | 停止服务 |
| `./deploy.sh restart` | 重启服务 |
| `./deploy.sh logs` | 查看日志 |
| `./deploy.sh status` | 查看服务状态 |
| `./deploy.sh build` | 仅构建镜像 |
| `./deploy.sh clean` | 清理容器和数据卷 |

---

## 🏗️ 架构说明

```
                    ┌─────────────────┐
                    │     用户浏览器    │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │  Nginx (前端)     │  :80
                    │  / → 静态文件     │
                    │  /api/* → 后端   │
                    └────────┬─────────┘
                             │
              ┌─────────────┴─────────────┐
              │                           │
     ┌────────▼─────────┐      ┌──���─────▼─────────┐
     │   Spring Boot     │      │    MySQL 8.0      │
     │   (后端 :3000)    │      │    (:3307)        │
     └────────┬─────────┘      └───────────────────┘
              │
     ┌────────▼─────────┐
     │    Redis 7       │
     │    (:6380)       │
     └───────────────────┘
```

### 容器说明

| 容器名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| aitrainer-frontend | nginx:alpine | 80 | 前端静态资源 + API代理 |
| aitrainer-backend | maven:3.9-eclipse-temurin-17 | 3000 | Spring Boot 后端服务 |
| aitrainer-mysql | mysql:8.0 | 3307 | MySQL 数据库 |
| aitrainer-redis | redis:7-alpine | 6380 | Redis 缓存 |

---

## 🔧 Nginx 网络分流配置

前端 Nginx 配置文件 (`frontend/nginx.conf`) 实现了以下路由规则：

### 路由规则

| 路径 | 目标服务 | 说明 |
|------|---------|------|
| `/` | 静态文件 | Vue 前端页面 |
| `/api/*` | backend:3000 | API 请求代理 |
| `/swagger-ui/*` | backend:3000 | API 文档 |
| `/v3/api-docs` | backend:3000 | OpenAPI 规范 |

### 主要特性

1. **Gzip 压缩** - 启用压缩减少传输体积
2. **静态资源缓存** - JS/CSS/图片等长期缓存
3. **请求头转发** - 正确传递 X-Real-IP 等信息
4. **超时设置** - 60s 超时防止长请求阻塞
5. **SPA 路由支持** - Vue Router history 模式支持

---

## 🔐 环境变量说明

### 数据库配置
```env
MYSQL_ROOT_PASSWORD=AiTrainer_Admin_2026
MYSQL_DB=ai_trainer
MYSQL_USER=aitrainer_user
MYSQL_PASSWORD=AiTrainer_Pass_2026
MYSQL_PORT=3307
```

### Redis 配置
```env
REDIS_PORT=6380
REDIS_PASSWORD=AiTrainer_Redis_2026
```

### 服务端口
```env
BACKEND_PORT=3000
FRONTEND_PORT=80
```

### 邮件配置（QQ邮箱示例）
```env
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your授权码
```

### 阿里云 OSS
```env
ALIYUN_OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_BUCKET=your-bucket-name
ALIYUN_ACCESS_KEY_ID=your-access-key-id
ALIYUN_ACCESS_KEY_SECRET=your-access-key-secret
```

### AI 模型配置
```env
AI_API_KEY=your-openai-api-key
AI_MODEL=gpt-5.4
AI_BASE_URL=https://api.openai.com/v1/
```

---

## 🐛 故障排查

### 查看日志
```bash
docker compose logs -f
```

### 查看容器状态
```bash
docker compose ps
```

### 重建某个服务
```bash
docker compose up -d --force-recreate <服务名>
```

### 进入容器调试
```bash
docker exec -it aitrainer-backend /bin/sh
```

### 清理并重新部署
```bash
docker compose down -v
./deploy.sh start
```

---

## 📊 健康检查

各服务均配置了健康检查：

- **Backend**: `curl http://localhost:3000/actuator/health`
- **Frontend**: `wget http://localhost/`  
- **MySQL**: `mysqladmin ping`
- **Redis**: `redis-cli ping`

---

## 🌐 生产环境注意事项

1. **修改默认密码** - 务必修改 `.env` 中的所有密码
2. **配置 HTTPS** - 使用 Nginx 反向代理配置 SSL 证书
3. **限制端口访问** - 使用防火墙规则限制外部访问
4. **日志管理** - 配置日志轮转防止磁盘占满
5. **数据备份** - 定期备份 MySQL 和 Redis 数据

### HTTPS 配置示例
```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    # 后续配置与 HTTP server 相同
}
```
