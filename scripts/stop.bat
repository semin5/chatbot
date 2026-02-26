@echo off

set CONTAINER_NAME=chatbot-container

echo Stopping and removing container...
docker rm -f %CONTAINER_NAME% 2>nul

echo Done.
pause