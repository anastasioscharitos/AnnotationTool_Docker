@echo off
title Annotation Tool Launcher

echo [1/4] Start Docker Desktop...
docker info > nul 2>&1
if %errorlevel% neq 0 (
	echo The Docker is not running....
	start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
)

:waitloop
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Wait Docker to start...
    timeout /t 3 /nobreak >nul
    goto waitloop
)

echo [2/4] Start Containers (Java + Python)
docker-compose up -d

echo [3/4] wait UI to start
timeout /t 15 /nobreak >nul

echo [4/4] Open browser
start http://localhost:8080

echo The web app is running
echo don' t close this window
echo press any key to exit

pause

echo Stop web app
docker-compose down
echo this is the end
timeout /t 3
exit