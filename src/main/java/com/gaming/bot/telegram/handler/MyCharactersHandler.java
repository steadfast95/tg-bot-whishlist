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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyCharactersHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/mycharacters");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();

        try {
            User user = userService.getUserByTelegramId(telegramId);
            List<Character> characters = characterService.getUserCharacters(user);

            if (characters.isEmpty()) {
                bot.sendMessage(chatId, "У вас пока нет персонажей. Создайте первого командой /createcharacter");
                return;
            }

            String response = String.format("""
                    👥 Ваши персонажи (%d)
                    
                    Выберите персонажа для просмотра подробной информации:
                    """, characters.size());

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharactersListKeyboard(characters));
        } catch (Exception e) {
            log.error("Error getting characters", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении списка персонажей: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/mycharacters";
    }
}
