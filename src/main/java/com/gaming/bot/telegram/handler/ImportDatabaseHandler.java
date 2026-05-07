package com.gaming.bot.telegram.handler;

import com.gaming.bot.config.BotConfig;
import com.gaming.bot.service.DatabaseBackupService;
import com.gaming.bot.service.UserStateService;
import com.gaming.bot.telegram.CharacterGroupBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImportDatabaseHandler implements CommandHandler {

    private final DatabaseBackupService backupService;
    private final UserStateService userStateService;
    private final BotConfig.BotProperties botProperties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String getCommand() {
        return "/importdb";
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage()) {
            return false;
        }

        // Обработка команды /importdb
        if (update.getMessage().hasText() &&
                update.getMessage().getText().startsWith(getCommand())) {
            return true;
        }

        // Обработка файла в состоянии ожидания импорта
        if (update.getMessage().hasDocument()) {
            Long chatId = update.getMessage().getChatId();
            return userStateService.getState(chatId) == UserStateService.State.WAITING_FOR_IMPORT;
        }

        return false;
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();

        // Если это команда — проверяем секрет и ожидаем файл
        if (update.getMessage().hasText()) {
            handleCommand(update, bot, chatId);
            return;
        }

        // Если это файл — выполняем импорт
        if (update.getMessage().hasDocument()) {
            handleFileUpload(update, bot, chatId);
        }
    }

    private void handleCommand(Update update, CharacterGroupBot bot, Long chatId) {
        String text = update.getMessage().getText();
        String[] parts = text.split("\\s+", 2);

        if (parts.length < 2) {
            bot.sendMessage(chatId, "❌ Использование: /importdb <секретный_ключ>\n\n" +
                    "Секретный ключ можно указать в конфиге (ADMIN_SECRET) или он генерируется автоматически из токена бота.");
            return;
        }

        String providedSecret = parts[1].trim();
        String expectedSecret = botProperties.getEffectiveAdminSecret();

        if (!providedSecret.equals(expectedSecret)) {
            log.warn("Invalid admin secret attempt from chat: {}", chatId);
            bot.sendMessage(chatId, "❌ Неверный секретный ключ");
            return;
        }

        userStateService.setState(chatId, UserStateService.State.WAITING_FOR_IMPORT);
        bot.sendMessage(chatId, "📁 Отправьте JSON-файл бэкапа для импорта.\n\n" +
                "⚠️ ВНИМАНИЕ: Импорт полностью заменит все существующие данные!\n\n" +
                "Для отмены отправьте /cancel");
    }

    private void handleFileUpload(Update update, CharacterGroupBot bot, Long chatId) {
        Document document = update.getMessage().getDocument();

        // Проверяем что это JSON файл
        String fileName = document.getFileName();
        if (fileName == null || !fileName.toLowerCase().endsWith(".json")) {
            bot.sendMessage(chatId, "❌ Пожалуйста, отправьте JSON-файл (.json)");
            return;
        }

        try {
            log.info("Import database requested by chat: {}, file: {}", chatId, fileName);

            // Скачиваем файл
            String fileId = document.getFileId();
            org.telegram.telegrambots.meta.api.objects.File telegramFile = bot.getFile(fileId);

            String fileUrl = "https://api.telegram.org/file/bot" + botProperties.getToken()
                    + "/" + telegramFile.getFilePath();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String json = response.body();

            if (json == null || json.isBlank()) {
                bot.sendMessage(chatId, "❌ Файл пустой или не удалось прочитать");
                return;
            }

            // Выполняем импорт
            DatabaseBackupService.ImportResult result = backupService.importFromJson(json);

            userStateService.clearState(chatId);

            String message = String.format("""
                    ✅ База данных успешно восстановлена!
                    
                    📊 Импортировано:
                    👤 Пользователей: %d
                    🎭 Персонажей: %d
                    👥 Групп: %d
                    🤝 Участников групп: %d
                    ⚔️ Экипировки: %d
                    ⭐ Желаний: %d
                    
                    📅 Бэкап был создан: %s
                    """,
                    result.getUsersCount(),
                    result.getCharactersCount(),
                    result.getGroupsCount(),
                    result.getGroupMembersCount(),
                    result.getEquipmentCount(),
                    result.getWishlistsCount(),
                    result.getExportedAt() != null 
                            ? result.getExportedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                            : "неизвестно"
            );

            bot.sendMessage(chatId, message);
            log.info("Database import completed successfully for chat: {}", chatId);

        } catch (Exception e) {
            log.error("Failed to import database", e);
            userStateService.clearState(chatId);
            bot.sendMessage(chatId, "❌ Ошибка импорта: " + e.getMessage() + 
                    "\n\nПроверьте что файл является корректным бэкапом.");
        }
    }
}
