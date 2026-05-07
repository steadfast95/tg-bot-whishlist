package com.gaming.bot.telegram.handler;

import com.gaming.bot.service.UserStateService;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CancelHandler implements CommandHandler {

    private final UserStateService userStateService;

    @Override
    public String getCommand() {
        return "/cancel";
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
        userStateService.clearState(chatId);
        bot.sendMessageWithKeyboard(chatId, "❌ Операция отменена", KeyboardFactory.getMainMenuKeyboard());
    }
}
