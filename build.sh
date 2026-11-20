#!/bin/bash
echo "Building Cosmetics Plugin..."
echo

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found in PATH"
    echo "Please install Maven and add it to your PATH"
    exit 1
fi

# Check for Java 21
JAVA_VERSION=$(java -version 2>&1 | head -1 | grep -oE '[0-9]+\.[0-9]+' | head -1)
if [[ "$JAVA_VERSION" != "21"* ]]; then
    echo "WARNING: Java 21 not detected. This plugin requires Java 21."
    echo "Make sure JAVA_HOME points to Java 21."
    echo
fi

echo "Compiling with Maven..."
mvn clean package

if [ $? -ne 0 ]; then
    echo
    echo "BUILD FAILED"
    exit 1
fi

echo
echo "BUILD SUCCESSFUL!"
echo "JAR location: target/Cosmetics-1.0.0.jar"