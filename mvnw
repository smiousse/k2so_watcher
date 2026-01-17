#!/bin/sh
# Maven Wrapper script
# Licensed to the Apache Software Foundation (ASF)

set -e

# Resolve the directory containing this script
if [ -z "$MAVEN_WRAPPER_PROPERTIES" ]; then
    MAVEN_WRAPPER_PROPERTIES="$(dirname "$0")/.mvn/wrapper/maven-wrapper.properties"
fi

if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    distributionUrl=$(grep -E "^distributionUrl=" "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi

if [ -z "$distributionUrl" ]; then
    distributionUrl="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
fi

# Determine Maven home
if [ -z "$MAVEN_HOME" ]; then
    MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.6"
fi

# Download and install Maven if not present
if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven..."
    mkdir -p "$MAVEN_HOME"
    tmpFile="$(mktemp)"
    curl -fsSL "$distributionUrl" -o "$tmpFile"
    unzip -q -d "$MAVEN_HOME" "$tmpFile"
    rm "$tmpFile"
    mv "$MAVEN_HOME"/apache-maven-*/* "$MAVEN_HOME"
fi

# Run Maven
exec "$MAVEN_HOME/bin/mvn" "$@"
