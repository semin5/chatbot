set -eu

PORT="${PORT:-8081}"

JAVA_OPTS="${JAVA_OPTS:-}"

SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-}"

APP_OPTS="${APP_OPTS:-}"

CMD="java ${JAVA_OPTS} \
  -Dserver.port=${PORT}"

if [ -n "${SPRING_PROFILES_ACTIVE}" ]; then
  CMD="${CMD} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}"
fi

CMD="${CMD} org.springframework.boot.loader.launch.JarLauncher ${APP_OPTS}"

if [ "$#" -gt 0 ]; then
  CMD="${CMD} $*"
fi

echo "Starting app on port ${PORT}..."
echo "CMD: ${CMD}"

exec sh -c "${CMD}"