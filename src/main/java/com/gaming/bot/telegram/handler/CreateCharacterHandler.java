package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.User;
import com.gaming.bot.service.CharacterService;
import com.gaming.bot.service.UserService;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateCharacterHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/createcharacter");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split("\\|", 5);
        
        if (parts.length < 2) {
            String usage = """
                    📝 Создание персонажа
                    
                    Формат: /createcharacter имя|класс|уровень|описание
                    
                    Пример:
                    /createcharacter Арагорн|Воин|15|Следопыт Севера
                    
                    Минимум нужно указать имя:
                    /createcharacter Арагорн
                    """;
            bot.sendMessage(chatId, usage);
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            
            String name = parts[0].replace("/createcharacter", "").trim();
            String characterClass = parts.length > 1 ? parts[1].trim() : null;
            Integer level = parts.length > 2 ? parseInteger(parts[2].trim()) : 1;
            String description = parts.length > 3 ? parts[3].trim() : null;

            Character character = characterService.createCharacter(
                    user, name, characterClass, level, description
            );

            String response = String.format("""
                    ✅ Персонаж создан!
                    
                    👤 Имя: %s
                    ⚔️ Класс: %s
                    📊 Уровень: %d
                    📝 Описание: %s
                    🆔 ID: %d
                    """,
                    character.getName(),
                    character.getCharacterClass() != null ? character.getCharacterClass() : "Не указан",
                    character.getLevel(),
                    character.getDescription() != null ? character.getDescription() : "Нет описания",
                    character.getId()
            );

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharactersMenuKeyboard());
        } catch (Exception e) {
            log.error("Error creating character", e);
            bot.sendMessage(chatId, "❌ Ошибка при создании персонажа: " + e.getMessage());
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public String getCommand() {
        return "/createcharacter";
    }
}
