#!/bin/bash

set -eo pipefail

if [[ -z "$BOOT_JAR" ]]; then
  echo "BOOT_JAR environment variable must be specified" && exit 1
fi

if [[ -z "$SPRING_PROFILES_ACTIVE" ]]; then
  SPRING_PROFILES_ACTIVE="docker"
else
  SPRING_PROFILES_ACTIVE="docker,${SPRING_PROFILES_ACTIVE}"
fi

if [[ -n "$DEBUG" && "$DEBUG"="true" ]]; then
  echo "Running the application in debug mode"
  JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=$DEBUG_PORT,suspend=n"
fi

# Check WAIT_FOR_IT environment variable
if [[ -n "$WAIT_FOR_IT" ]]; then
  ./wait-for-it.sh "$WAIT_FOR_IT"
fi
# Check WAIT_FOR_IT_${ct} in case we have more than one service to wait on
ct=0
while (true); do
  params='WAIT_FOR_IT_'${ct}''
  if [[ -z ${!params:x} ]]; then
    break
  else
    ./wait-for-it.sh ${!params}
  fi
  ct=$(( ct + 1 ))
done

echo "Starting Spring Boot service - /opt/app/$BOOT_JAR, profiles=$SPRING_PROFILES_ACTIVE"

# Enables application to take PID 1 and receive SIGTERM sent by Docker stop command.
# See here https://docs.docker.com/engine/reference/builder/#/entrypoint
exec java ${JAVA_OPTS} \
 -Djava.security.egd=file:/dev/./urandom \
 -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" -jar /opt/app/${BOOT_JAR}
