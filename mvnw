#!/bin/sh
# Maven Wrapper - minimal POSIX shell launcher
# If system mvn is available, use it; else try to download wrapper jar.
set -e
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPS="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi
if [ -r "$WRAPPER_JAR" ]; then
  if command -v java >/dev/null 2>&1; then
    exec java ${MAVEN_OPTS} -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
  else
    echo "Error: Java not found and no system mvn available." >&2
    exit 1
  fi
fi
# No mvn and no wrapper jar: try to download distribution directly if curl/wget available
if [ -f "$WRAPPER_PROPS" ]; then
  DIST_URL=$(grep 'distributionUrl' "$WRAPPER_PROPS" | cut -d'=' -f2- | tr -d ' \r')
  echo "Maven not found. Attempting to download from $DIST_URL" >&2
  DIST_ZIP="/tmp/maven-dist.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$DIST_ZIP" "$DIST_URL" || { echo "curl failed"; exit 1; }
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$DIST_ZIP" "$DIST_URL" || { echo "wget failed"; exit 1; }
  else
    echo "Need curl or wget to download Maven." >&2
    exit 1
  fi
  unzip -q -o "$DIST_ZIP" -d /tmp
  MAVEN_HOME=$(find /tmp -maxdepth 1 -name "apache-maven-*" -type d | head -n1)
  exec "$MAVEN_HOME/bin/mvn" "$@"
fi
echo "Error: Maven not found. Install JDK 17 and Maven, or add .mvn/wrapper/maven-wrapper.jar" >&2
exit 1
