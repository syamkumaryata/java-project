#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Unix shell script
# ----------------------------------------------------------------------------

export MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$PWD"}
BASE_DIR="$PWD"
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

if [ -z "$JAVA_HOME" ] ; then
  JAVACMD="java"
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$BASE_DIR/.mvn/wrapper"
  curl -fsSL -o "$WRAPPER_JAR" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" || \
  wget -q -O "$WRAPPER_JAR" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
fi

exec "$JAVACMD" $MAVEN_OPTS -cp "$WRAPPER_JAR" "$WRAPPER_LAUNCHER" "$@"
