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
public class EditCharacterHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/editcharacter");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split("\\|", 6);
        
        if (parts.length < 2) {
            String usage = """
                    ✏️ Редактирование персонажа
                    
                    Формат: /editcharacter ID|имя|класс|уровень|описание
                    
                    Пример:
                    /editcharacter 1|Арагорн|Воин|20|Король Гондора
                    
                    Минимум: /editcharacter 1|Новое имя
                    
                    Используйте /mycharacters чтобы узнать ID персонажа
                    """;
            bot.sendMessage(chatId, usage);
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            
            Long characterId = Long.parseLong(parts[0].replace("/editcharacter", "").trim());
            String name = parts[1].trim();
            String characterClass = parts.length > 2 ? parts[2].trim() : null;
            Integer level = parts.length > 3 ? parseInteger(parts[3].trim()) : null;
            String description = parts.length > 4 ? parts[4].trim() : null;

            Character oldChar = characterService.getCharacterById(characterId, user);
            
            Character character = characterService.updateCharacter(
                    characterId, 
                    user, 
                    name.isEmpty() ? oldChar.getName() : name,
                    characterClass != null && !characterClass.isEmpty() ? characterClass : oldChar.getCharacterClass(),
                    level != null ? level : oldChar.getLevel(),
                    description != null && !description.isEmpty() ? description : oldChar.getDescription()
            );

            String response = String.format("""
                    ✅ Персонаж обновлен!
                    
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
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID персонажа");
        } catch (Exception e) {
            log.error("Error editing character", e);
            bot.sendMessage(chatId, "❌ Ошибка при редактировании персонажа: " + e.getMessage());
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getCommand() {
        return "/editcharacter";
    }
}
