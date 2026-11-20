@echo off
echo Building Cosmetics Plugin...
echo.

REM Check for Maven
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven not found in PATH
    echo Please install Maven and add it to your PATH
    pause
    exit /b 1
)

REM Check for Java 21
java -version 2>&1 | findstr /r "21\." >nul
if %errorlevel% neq 0 (
    echo WARNING: Java 21 not detected. This plugin requires Java 21.
    echo Make sure JAVA_HOME points to Java 21.
    echo.
)

echo Compiling with Maven...
mvn clean package

if %errorlevel% neq 0 (
    echo.
    echo BUILD FAILED
    pause
    exit /b 1
)

echo.
echo BUILD SUCCESSFUL!
echo JAR location: target/Cosmetics-1.0.0.jar
echo.
pause