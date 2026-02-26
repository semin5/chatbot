@echo off

set IMAGE_NAME=chatbot
set CONTAINER_NAME=chatbot-container
set PORT=8081

echo Removing existing container...
docker rm -f %CONTAINER_NAME% 2>nul

echo Building Docker image...
docker build --no-cache -t %IMAGE_NAME%:latest .

echo Running container...
docker run --d ^
  --name %CONTAINER_NAME% ^
  -p %PORT%:%PORT% ^
  -e PORT=%PORT% ^
  --env-file .env.properties ^
  %IMAGE_NAME%:latest