package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.User;
import com.gaming.bot.service.GroupService;
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
public class DeleteGroupHandler implements CommandHandler {

    private final UserService userService;
    private final GroupService groupService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/deletegroup");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID группы: /deletegroup 1");
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            Long groupId = Long.parseLong(parts[1].trim());

            groupService.deleteGroup(groupId, user);

            String response = """
                    ✅ Группа удалена!
                    
                    Группа деактивирована, все участники исключены.
                    """;

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupsMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID группы");
        } catch (Exception e) {
            log.error("Error deleting group", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении группы: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/deletegroup";
    }
}
