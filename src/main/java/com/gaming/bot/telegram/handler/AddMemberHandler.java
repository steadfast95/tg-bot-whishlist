package com.gaming.bot.telegram.handler;

import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddMemberHandler implements CommandHandler {

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/addmember");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();

        String message = """
                👥 Добавление участников
                
                Теперь добавление участников работает через коды приглашения!
                
                📋 Как пригласить игрока:
                1. Откройте группу в меню /mygroups
                2. Нажмите "Пригласить (код)" - получите код
                3. Отправьте код игроку
                4. Игрок вводит /join КОД
                
                ⏱️ Код действует 15 минут.
                """;

        bot.sendMessageWithKeyboard(chatId, message, KeyboardFactory.getGroupsMenuKeyboard());
    }

    @Override
    public String getCommand() {
        return "/addmember";
    }
}
