@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo 正在启动蓝影电影评论网站...
echo 首次启动需要下载 Maven 依赖，请稍候。
call mvn spring-boot:run
if errorlevel 1 pause
