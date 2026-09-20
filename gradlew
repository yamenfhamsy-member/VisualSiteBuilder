#!/bin/sh
# Gradle wrapper launcher for VisualSiteBuilder.
# Uses the wrapper jar + properties committed under gradle/wrapper/.

APP_HOME=$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
  echo "Missing $CLASSPATH. The wrapper jar must be committed." >&2
  exit 1
fi

exec java -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
