#!/bin/bash
# =============================================================================
# AiTrainer 一键部署脚本
# 使用方法: ./deploy.sh [dev|start|stop|restart|logs|clean]
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_msg() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 进入部署目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 检查环境
check_env() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    # 检查 .env 文件
    if [ ! -f ".env" ]; then
        print_warn ".env 文件不存在，复制 .env.example 作为模板"
        cp .env.example .env
        print_warn "请编辑 .env 文件填入实际配置"
        exit 1
    fi
}

# 拉取最新代码
pull_code() {
    print_msg "拉取最新代码..."
    cd "$SCRIPT_DIR/.."
    git pull origin main
    cd "$SCRIPT_DIR"
}

# 构建镜像
build_images() {
    print_msg "构建 Docker 镜像..."
    docker compose build --no-cache
    print_msg "镜像构建完成"
}

# 启动服务
start_services() {
    print_msg "启动服务..."
    docker compose up -d
    print_msg "服务启动完成"
}

# 停止服务
stop_services() {
    print_msg "停止服务..."
    docker compose down
    print_msg "服务已停止"
}

# 重启服务
restart_services() {
    stop_services
    start_services
}

# 查看日志
show_logs() {
    docker compose logs -f --tail=100
}

# 查看服务状态
show_status() {
    docker compose ps
}

# 清理
clean_services() {
    print_warn "清理容器和数据卷..."
    docker compose down -v --rmi local
    print_msg "清理完成"
}

# 完整部署
deploy() {
    check_env
    pull_code
    build_images
    start_services
    
    print_msg "等待服务启动..."
    sleep 10
    
    print_msg "检查服务状态..."
    show_status
    
    print_msg ""
    print_msg "============================================"
    print_msg "部署完成！"
    print_msg "前端地址: http://localhost:80"
    print_msg "API文档: http://localhost/swagger-ui/index.html"
    print_msg "============================================"
}

# 主命令处理
case "${1:-start}" in
    dev)
        print_msg "开发模式启动（不构建镜像）..."
        check_env
        docker compose up -d
        ;;
    start)
        deploy
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        show_logs
        ;;
    status)
        show_status
        ;;
    build)
        build_images
        ;;
    clean)
        clean_services
        ;;
    pull)
        pull_code
        ;;
    *)
        echo "用法: $0 {dev|start|stop|restart|logs|status|build|clean|pull}"
        echo ""
        echo "命令说明:"
        echo "  dev     - 开发模式启动（使用现有镜像）"
        echo "  start   - ��整部署（拉取代码 + 构建 + 启动）"
        echo "  stop    - 停止服务"
        echo "  restart - 重启服务"
        echo "  logs    - 查看日志"
        echo "  status  - 查看服务状态"
        echo "  build   - 仅构建镜像"
        echo "  clean   - 清理容器和数据卷"
        echo "  pull    - 拉取最新代码"
        exit 1
        ;;
esac
