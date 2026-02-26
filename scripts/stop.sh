#!/bin/bash
set -e

CONTAINER_NAME="chatbot-container"

echo "Stopping and removing container..."
docker rm -f ${CONTAINER_NAME} 2>/dev/null || true

echo "Done."