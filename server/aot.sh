#!/usr/bin/env bash
set -e

# Build the application, generate the AOT cache, and start it with AOT optimization
# Use -b to only perform the build steps
# Use -s to only start the application

PROJECT_NAME="restbucks"
VERSION="1.0.0-SNAPSHOT"
TARGET="target/aot"
ARTIFACT="${PROJECT_NAME}-${VERSION}.jar"

# Change JAVA_OPTS to "" to not use Spring AOT optimizations
JAVA_OPTS="-Dspring.aot.enabled=true"

if [[ $1 != "-s" ]]; then

  if [ ! -f ${ARTIFACT} ]; then
    ./mvnw clean package -Paot -DskipTests
  fi

  # Unpack the Spring Boot executable JAR in a way suitable for optimal performances with AOT
  java -Djarmode=tools -jar "target/${ARTIFACT}" extract --destination ${TARGET}

  # AOT training run
  java -XX:AOTCacheOutput=${TARGET}/app.aot -Dspring.context.exit=onRefresh -jar "${TARGET}/${ARTIFACT}"
fi

if [[ $1 != "-b" ]]; then

  # AOT optimized run
  java -XX:AOTCache=${TARGET}/app.aot -jar "${TARGET}/${ARTIFACT}"
fi
