#!/usr/bin/env bash
set -e

# Build the application, generate the AppCDS cache, and start it with AppCDS optimization
# Use -b to only perform the build steps
# Use -s to only start the application

PROJECT_NAME="restbucks"
VERSION="1.0.0-SNAPSHOT"
TARGET="target/cds"

# Change JAVA_OPTS to "" to not use Spring AOT optimizations
JAVA_OPTS="-Dspring.aot.enabled=true"

if [[ $1 != "-s" ]]; then
  if [ ! -f target/${PROJECT_NAME}-${VERSION}.jar ]; then
    ./mvnw clean package -Paot -DskipTests
  fi

  # Unpack the Spring Boot executable JAR in a way suitable for optimal performances with AppCDS
  ./unpack-executable-jar.sh -d ${TARGET} target/${PROJECT_NAME}-${VERSION}.jar

  # AppCDS training run
  java $JAVA_OPTS -Dspring.context.exit=onRefresh -XX:ArchiveClassesAtExit=${TARGET}/application.jsa -jar ${TARGET}/run-app.jar
fi

if [[ $1 != "-b" ]]; then
  # CDS optimized run
  java $JAVA_OPTS -XX:SharedArchiveFile=${TARGET}/application.jsa -jar ${TARGET}/run-app.jar
fi
