#!/bin/bash
#
# Build Distribution Script
# Создает портативные дистрибутивы для macOS и Windows с встроенным JRE
#
# Использование:
#   ./build-dist.sh          - собрать для всех платформ
#   ./build-dist.sh macos    - только для macOS
#   ./build-dist.sh windows  - только для Windows
#

set -e

# Конфигурация
APP_NAME="character-group-bot"
APP_VERSION="1.0.0"
JDK_VERSION="21"
TEMURIN_VERSION="21.0.4+7"

# Директории
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/target"
DIST_DIR="$BUILD_DIR/dist"
JRE_CACHE_DIR="$BUILD_DIR/jre-cache"

# Поиск Maven
find_maven() {
    # 1. Проверяем Maven Wrapper в проекте
    if [ -x "$SCRIPT_DIR/mvnw" ]; then
        echo "$SCRIPT_DIR/mvnw"
        return
    fi
    
    # 2. Проверяем mvn в PATH
    if command -v mvn &> /dev/null; then
        echo "mvn"
        return
    fi
    
    # 3. Проверяем стандартные пути установки
    local maven_paths=(
        "/opt/apache-maven-3.9.1/bin/mvn"
        "/opt/maven/bin/mvn"
        "/usr/local/maven/bin/mvn"
        "/usr/local/bin/mvn"
        "$HOME/.sdkman/candidates/maven/current/bin/mvn"
    )
    
    for path in "${maven_paths[@]}"; do
        if [ -x "$path" ]; then
            echo "$path"
            return
        fi
    done
    
    # 4. Ищем через mdfind (macOS Spotlight)
    if command -v mdfind &> /dev/null; then
        local found=$(mdfind -name "mvn" 2>/dev/null | grep -E "bin/mvn$" | head -1)
        if [ -x "$found" ]; then
            echo "$found"
            return
        fi
    fi
    
    echo ""
}

MVN_CMD=$(find_maven)
if [ -z "$MVN_CMD" ]; then
    echo "Ошибка: Maven не найден!"
    echo "Установите Maven или создайте Maven Wrapper: mvn wrapper:wrapper"
    exit 1
fi

# URL для скачивания JRE (Eclipse Temurin)
# https://adoptium.net/temurin/releases/
TEMURIN_BASE_URL="https://github.com/adoptium/temurin21-binaries/releases/download"
TEMURIN_TAG="jdk-21.0.4%2B7"

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Функция скачивания JRE
download_jre() {
    local platform=$1
    local arch=$2
    local output_dir=$3
    
    local archive_ext="tar.gz"
    local jre_dir_name="jdk-${TEMURIN_VERSION}-jre"
    
    if [ "$platform" = "windows" ]; then
        archive_ext="zip"
    fi
    
    local filename="OpenJDK21U-jre_${arch}_${platform}_hotspot_${TEMURIN_VERSION//+/_}.${archive_ext}"
    local url="${TEMURIN_BASE_URL}/${TEMURIN_TAG}/${filename}"
    local cache_file="$JRE_CACHE_DIR/${filename}"
    
    mkdir -p "$JRE_CACHE_DIR"
    mkdir -p "$output_dir"
    
    # Скачиваем если нет в кеше
    if [ ! -f "$cache_file" ]; then
        log_info "Скачивание JRE для ${platform}-${arch}..."
        log_info "URL: $url"
        if ! curl -L -f -o "$cache_file" "$url"; then
            log_error "Не удалось скачать JRE"
            rm -f "$cache_file"
            return 1
        fi
    else
        log_info "Используем кешированный JRE для ${platform}-${arch}"
    fi
    
    # Распаковываем
    log_info "Распаковка JRE..."
    if [ "$archive_ext" = "zip" ]; then
        unzip -q "$cache_file" -d "$output_dir"
    else
        tar -xzf "$cache_file" -C "$output_dir"
    fi
    
    # Переименовываем директорию в runtime
    local extracted_dir=$(ls -d "$output_dir"/jdk-* 2>/dev/null | head -1)
    if [ -d "$extracted_dir" ]; then
        mv "$extracted_dir" "$output_dir/runtime"
    fi
}

# Функция сборки дистрибутива
build_distribution() {
    local platform=$1
    local arch=$2
    
    log_info "========================================="
    log_info "Сборка дистрибутива для ${platform}-${arch}"
    log_info "========================================="
    
    local dist_name="${APP_NAME}-${APP_VERSION}-${platform}-${arch}"
    local dist_path="$DIST_DIR/${dist_name}"
    
    # Очищаем директорию
    rm -rf "$dist_path"
    mkdir -p "$dist_path"
    
    # Создаем структуру директорий
    mkdir -p "$dist_path/app"
    mkdir -p "$dist_path/bin"
    mkdir -p "$dist_path/config"
    
    # Копируем JAR
    local jar_file="$BUILD_DIR/${APP_NAME}-${APP_VERSION}-SNAPSHOT.jar"
    if [ ! -f "$jar_file" ]; then
        # Пробуем без -SNAPSHOT
        jar_file="$BUILD_DIR/${APP_NAME}-${APP_VERSION}.jar"
    fi
    if [ ! -f "$jar_file" ]; then
        # Ищем любой JAR
        jar_file=$(ls "$BUILD_DIR"/*.jar 2>/dev/null | grep -v original | head -1)
    fi
    
    if [ ! -f "$jar_file" ]; then
        log_error "JAR файл не найден. Сначала выполните: mvn clean package"
        exit 1
    fi
    
    log_info "Копирование JAR: $jar_file"
    cp "$jar_file" "$dist_path/app/${APP_NAME}.jar"
    
    # Копируем скрипты запуска
    if [ "$platform" = "windows" ]; then
        cp "$SCRIPT_DIR/src/dist/bin/run.bat" "$dist_path/bin/"
    else
        cp "$SCRIPT_DIR/src/dist/bin/run.sh" "$dist_path/bin/"
        chmod +x "$dist_path/bin/run.sh"
    fi
    
    # Скачиваем и распаковываем JRE
    download_jre "$platform" "$arch" "$dist_path"
    
    # Копируем пример конфигурации
    if [ -f "$SCRIPT_DIR/.env.example" ]; then
        cp "$SCRIPT_DIR/.env.example" "$dist_path/config/"
    fi
    
    # Создаем README
    create_readme "$dist_path" "$platform"
    
    # Создаем архив
    log_info "Создание архива..."
    cd "$DIST_DIR"
    if [ "$platform" = "windows" ]; then
        zip -r -q "${dist_name}.zip" "${dist_name}"
    else
        tar -czf "${dist_name}.tar.gz" "${dist_name}"
    fi
    cd "$SCRIPT_DIR"
    
    # Выводим информацию
    local archive_file
    if [ "$platform" = "windows" ]; then
        archive_file="$DIST_DIR/${dist_name}.zip"
    else
        archive_file="$DIST_DIR/${dist_name}.tar.gz"
    fi
    
    local size=$(du -h "$archive_file" | cut -f1)
    log_info "Готово: $archive_file ($size)"
}

# Функция создания README
create_readme() {
    local dist_path=$1
    local platform=$2
    
    cat > "$dist_path/README.txt" << 'EOF'
Character Group Bot
===================

Telegram bot for managing characters and groups.

GETTING STARTED
---------------
EOF

    if [ "$platform" = "windows" ]; then
        cat >> "$dist_path/README.txt" << 'EOF'

Windows:
  1. Open the bin folder
  2. Run run.bat

Or from command line:
  cd bin
  run.bat

EOF
    else
        cat >> "$dist_path/README.txt" << 'EOF'

macOS/Linux:
  1. Open Terminal
  2. Navigate to the application directory
  3. Run: ./bin/run.sh

Or:
  chmod +x bin/run.sh
  ./bin/run.sh

EOF
    fi

    cat >> "$dist_path/README.txt" << 'EOF'
CONFIGURATION
-------------

Before running, configure environment variables or config file:

1. Via environment variables:
   - BOT_NAME - your bot username
   - BOT_TOKEN - bot token from @BotFather
   - DATABASE_URL - PostgreSQL database URL (optional)
   - DATABASE_USERNAME - database user (optional)
   - DATABASE_PASSWORD - database password (optional)

2. Or create config/application.yml with settings.

REQUIREMENTS
------------

- No Java installation required - JRE is bundled
- PostgreSQL 12+ for production database (H2 used by default)

DIRECTORY STRUCTURE
-------------------

app/          - Application JAR file
bin/          - Launch scripts  
config/       - Configuration files
runtime/      - Bundled Java Runtime

TROUBLESHOOTING
---------------

If you encounter issues:
1. Check environment variable settings
2. Verify PostgreSQL database is accessible
3. Validate Telegram bot token
EOF
}

# Функция сборки JAR
build_jar() {
    log_info "Сборка JAR файла..."
    log_info "Используем Maven: $MVN_CMD"
    cd "$SCRIPT_DIR"
    "$MVN_CMD" clean package -DskipTests -q
    log_info "JAR файл собран"
}

# Основная функция
main() {
    local target=${1:-all}
    
    log_info "Character Group Bot - Build Distribution"
    log_info "Версия: $APP_VERSION"
    log_info "JRE: Temurin $TEMURIN_VERSION"
    log_info "Maven: $MVN_CMD"
    echo
    
    # Создаем директории
    mkdir -p "$DIST_DIR"
    mkdir -p "$JRE_CACHE_DIR"
    
    # Собираем JAR если нужно
    if [ ! -f "$BUILD_DIR"/*.jar ] || [ "$REBUILD" = "true" ]; then
        build_jar
    fi
    
    case $target in
        macos)
            build_distribution "mac" "aarch64"  # Apple Silicon
            build_distribution "mac" "x64"      # Intel
            ;;
        windows)
            build_distribution "windows" "x64"
            ;;
        all)
            build_distribution "mac" "aarch64"
            build_distribution "mac" "x64"
            build_distribution "windows" "x64"
            ;;
        *)
            log_error "Неизвестная платформа: $target"
            echo "Использование: $0 [macos|windows|all]"
            exit 1
            ;;
    esac
    
    echo
    log_info "========================================="
    log_info "Сборка завершена!"
    log_info "Дистрибутивы находятся в: $DIST_DIR"
    log_info "========================================="
    ls -lh "$DIST_DIR"/*.{zip,tar.gz} 2>/dev/null || true
}

# Запуск
main "$@"
