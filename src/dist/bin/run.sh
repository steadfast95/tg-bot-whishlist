#!/bin/bash
#
# Character Group Bot - macOS/Linux Launcher
# Запускает приложение с встроенным JRE
#

# Определяем директорию скрипта
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"

# Пути к компонентам
JAVA_HOME="$APP_HOME/runtime"
JAR_FILE="$APP_HOME/app/character-group-bot.jar"
CONFIG_DIR="$APP_HOME/config"

# Проверяем наличие JRE
if [ ! -d "$JAVA_HOME" ]; then
    echo "Ошибка: JRE не найден в $JAVA_HOME"
    exit 1
fi

# Проверяем наличие JAR файла
if [ ! -f "$JAR_FILE" ]; then
    echo "Ошибка: JAR файл не найден: $JAR_FILE"
    exit 1
fi

# Определяем исполняемый файл Java
JAVA_EXE="$JAVA_HOME/bin/java"
if [ ! -x "$JAVA_EXE" ]; then
    echo "Ошибка: Java не найдена или не исполняемая: $JAVA_EXE"
    exit 1
fi

# JVM параметры
JVM_OPTS="${JVM_OPTS:--Xms256m -Xmx512m}"

# Добавляем конфигурацию если есть
SPRING_OPTS=""
if [ -d "$CONFIG_DIR" ]; then
    SPRING_OPTS="--spring.config.additional-location=file:$CONFIG_DIR/"
fi

# Переходим в директорию приложения
cd "$APP_HOME"

# Запускаем приложение
echo "Запуск Character Group Bot..."
echo "Java: $JAVA_EXE"
echo "JAR: $JAR_FILE"

exec "$JAVA_EXE" $JVM_OPTS -jar "$JAR_FILE" $SPRING_OPTS "$@"
