#!/usr/bin/env sh
set -eu

: "${POSTGRE_PASSWORD:?POSTGRE_PASSWORD is required}"
: "${OPENAI:?OPENAI is required}"

case "${POSTGRE_URL:-}" in
  ""|jdbc:postgresql://localhost:5432/*)
    POSTGRE_URL="jdbc:postgresql://host.docker.internal:5432/chatbot"
    ;;
esac
: "${POSTGRE_USERNAME:=postgres}"

: "${REDIS_HOST:?REDIS_HOST is required}"
: "${REDIS_PORT:?REDIS_PORT is required}"
: "${REDIS_PASSWORD:=}"

export POSTGRE_URL
export POSTGRE_USERNAME
export POSTGRE_PASSWORD
export REDIS_HOST
export REDIS_PORT
export REDIS_PASSWORD
export OPENAI

PORT="${PORT:-8081}"
JAVA_OPTS="${JAVA_OPTS:-}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-}"
APP_OPTS="${APP_OPTS:-}"

CMD="java ${JAVA_OPTS} -Dserver.port=${PORT}"

if [ -n "${SPRING_PROFILES_ACTIVE}" ]; then
  CMD="${CMD} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}"
fi

CMD="${CMD} org.springframework.boot.loader.launch.JarLauncher ${APP_OPTS}"

if [ "$#" -gt 0 ]; then
  CMD="${CMD} $*"
fi

echo "Starting app on port ${PORT}..."

echo "POSTGRE_URL=${POSTGRE_URL}"
echo "REDIS=${REDIS_HOST}:${REDIS_PORT}"

echo "CMD: ${CMD}"

exec sh -c "${CMD}"