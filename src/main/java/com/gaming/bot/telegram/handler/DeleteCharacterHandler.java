package com.gaming.bot.telegram.handler;

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
public class DeleteCharacterHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/deletecharacter");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID персонажа: /deletecharacter 1");
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            Long characterId = Long.parseLong(parts[1].trim());

            characterService.deleteCharacter(characterId, user);

            String response = """
                    ✅ Персонаж удален!
                    
                    Персонаж деактивирован и больше не отображается в списке.
                    """;

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharactersMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID персонажа");
        } catch (Exception e) {
            log.error("Error deleting character", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении персонажа: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/deletecharacter";
    }
}
