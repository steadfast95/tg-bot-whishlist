@echo off
REM Character Group Bot - Windows Launcher
REM Runs the application with bundled JRE

setlocal enabledelayedexpansion

REM Get script directory
set "SCRIPT_DIR=%~dp0"
set "APP_HOME=%SCRIPT_DIR%.."

REM Component paths
set "JAVA_HOME=%APP_HOME%\runtime"
set "JAR_FILE=%APP_HOME%\app\character-group-bot.jar"
set "CONFIG_DIR=%APP_HOME%\config"

REM Check JRE exists
if not exist "%JAVA_HOME%" (
    echo Error: JRE not found at %JAVA_HOME%
    pause
    exit /b 1
)

REM Check JAR exists
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found: %JAR_FILE%
    pause
    exit /b 1
)

REM Java executable
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
    echo Error: Java not found: %JAVA_EXE%
    pause
    exit /b 1
)

REM JVM options (can be overridden via environment variable)
if "%JVM_OPTS%"=="" set "JVM_OPTS=-Xms256m -Xmx512m"

REM Add config location if exists
set "SPRING_OPTS="
if exist "%CONFIG_DIR%" (
    set "SPRING_OPTS=--spring.config.additional-location=file:%CONFIG_DIR%/"
)

REM Change to app directory
cd /d "%APP_HOME%"

REM Start application
echo Starting Character Group Bot...
echo Java: %JAVA_EXE%
echo JAR: %JAR_FILE%
echo.

"%JAVA_EXE%" %JVM_OPTS% -jar "%JAR_FILE%" %SPRING_OPTS% %*

REM Show message if app exited with error
if errorlevel 1 (
    echo.
    echo Application exited with error.
    pause
)

endlocal
