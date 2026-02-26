#!/bin/bash
set -e

IMAGE_NAME="chatbot"
CONTAINER_NAME="chatbot-container"
PORT="8081"

echo "Removing existing container..."
docker rm -f ${CONTAINER_NAME} 2>/dev/null || true

echo "Building Docker image..."
docker build --no-cache -t ${IMAGE_NAME}:latest .

echo "Running container in background..."
docker run -d \
  --name ${CONTAINER_NAME} \
  -p ${PORT}:${PORT} \
  -e PORT=${PORT} \
  --env-file .env.properties \
  ${IMAGE_NAME}:latest