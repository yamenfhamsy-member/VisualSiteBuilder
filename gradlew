#!/bin/sh
# Gradle wrapper script — delegates to the distribution declared in gradle/wrapper/gradle-wrapper.properties.
# No local Gradle install or build is performed by this file alone.

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd -P)

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ ! -f "$CLASSPATH" ]; then
  echo "Downloading gradle-wrapper.jar..."
  WRAPPER_JAR_URL="https://github.com/gradle/gradle/raw/master/gradle/wrapper/gradle-wrapper.jar"
  if command -v curl >/dev/null 2>&1; then
    curl -sL -o "$CLASSPATH" "$WRAPPER_JAR_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$CLASSPATH" "$WRAPPER_JAR_URL"
  else
    echo "curl or wget is required to download the wrapper jar." >&2
    exit 1
  fi
fi

exec java $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
