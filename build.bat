@echo off
chcp 65001 >nul
cd /d "%~dp0"
call mvn clean package
if errorlevel 1 pause
echo.
echo 构建完成：target\movie-review-site.jar
pause
