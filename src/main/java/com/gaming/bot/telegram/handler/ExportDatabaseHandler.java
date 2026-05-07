package com.gaming.bot.telegram.handler;

import com.gaming.bot.config.BotConfig;
import com.gaming.bot.service.DatabaseBackupService;
import com.gaming.bot.telegram.CharacterGroupBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportDatabaseHandler implements CommandHandler {

    private final DatabaseBackupService backupService;
    private final BotConfig.BotProperties botProperties;

    @Override
    public String getCommand() {
        return "/exportdb";
    }

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() &&
                update.getMessage().hasText() &&
                update.getMessage().getText().startsWith(getCommand());
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // Формат: /exportdb <secret>
        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2) {
            bot.sendMessage(chatId, "❌ Использование: /exportdb <секретный_ключ>\n\n" +
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

        try {
            log.info("Export database requested by chat: {}", chatId);
            String json = backupService.exportToJson();

            String filename = "backup_" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".json";

            SendDocument sendDocument = SendDocument.builder()
                    .chatId(chatId.toString())
                    .document(new InputFile(
                            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                            filename
                    ))
                    .caption("✅ Бэкап базы данных создан\n\n" +
                            "📅 Дата: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "\n" +
                            "📦 Для восстановления используйте /importdb")
                    .build();

            bot.sendDocument(sendDocument);
            log.info("Database export completed successfully for chat: {}", chatId);

        } catch (Exception e) {
            log.error("Failed to export database", e);
            bot.sendMessage(chatId, "❌ Ошибка экспорта: " + e.getMessage());
        }
    }
}
