@echo off
chcp 65001 >nul
REM =============================================================================
REM AiTrainer 一键部署脚本 (Windows)
REM 使用方法: deploy.bat [dev|start|stop|restart|logs|status|clean]
REM =============================================================================

setlocal enabledelayedexpansion

set "COMMANDS=dev start stop restart logs status clean"

if "%~1"=="" set "COMMAND=start"
if "%~1"=="dev" set "COMMAND=dev"
if "%~1"=="start" set "COMMAND=start"
if "%~1"=="stop" set "COMMAND=stop"
if "%~1"=="restart" set "COMMAND=restart"
if "%~1"=="logs" set "COMMAND=logs"
if "%~1"=="status" set "COMMAND=status"
if "%~1"=="clean" set "COMMAND=clean"

:execute
if "%COMMAND%"=="dev" goto dev
if "%COMMAND%"=="start" goto start
if "%COMMAND%"=="stop" goto stop
if "%COMMAND%"=="restart" goto restart
if "%COMMAND%"=="logs" goto logs
if "%COMMAND%"=="status" goto status
if "%COMMAND%"=="clean" goto clean
goto usage

:dev
echo [INFO] 开发模式启动（使用现有镜像）...
call :check_env
docker compose up -d
goto :eof

:start
echo [INFO] 完整部署...
call :check_env
call :pull_code
call :build_images
call :start_services
echo [INFO] 等待服务启动...
timeout /t 10 /nobreak >nul
call :show_status
echo.
echo ============================================
echo 部署完成！
echo 前端地址: http://localhost:80
echo API文档: http://localhost/swagger-ui/index.html
echo ============================================
goto :eof

:stop
echo [INFO] 停止服务...
docker compose down
echo [INFO] 服务已停止
goto :eof

:restart
echo [INFO] 重启服务...
docker compose restart
goto :eof

:logs
docker compose logs -f --tail=100
goto :eof

:status
docker compose ps
goto :eof

:build
echo [INFO] 构建 Docker 镜像...
docker compose build --no-cache
echo [INFO] 镜像构建完成
goto :eof

:clean
echo [WARN] 清理容器和数据卷...
docker compose down -v --rmi local
echo [INFO] 清理完成
goto :eof

:pull_code
echo [INFO] 拉取最新代码...
git pull origin main
goto :eof

:build_images
echo [INFO] 构建 Docker 镜像...
docker compose build --no-cache
echo [INFO] 镜像构建完成
goto :eof

:start_services
echo [INFO] 启动服务...
docker compose up -d
echo [INFO] 服务启动完成
goto :eof

:show_status
docker compose ps
goto :eof

:check_env
where docker >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker 未安装，请先安装 Docker
    exit /b 1
)
where docker-compose >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Docker Compose 未安装
        exit /b 1
    )
)
if not exist ".env" (
    echo [WARN] .env 文件不存在，复制 .env.example 作为模板
    copy .env.example .env
    echo [WARN] 请编辑 .env 文件填入实际配置
    exit /b 1
)
goto :eof

:usage
echo 用法: %~nx0 {dev^|start^|stop^|restart^|logs^|status^|build^|clean}
echo.
echo 命令说明:
echo   dev     - 开发模式启动（使用现有镜像）
echo   start   - 完整部署（拉取代码 + 构建 + 启动）
echo   stop    - 停止服务
echo   restart - 重启服务
echo   logs    - 查看日志
echo   status  - 查看服务状态
echo   build   - 仅构建镜像
echo   clean   - 清理容器和数据卷
exit /b 1

endlocal
