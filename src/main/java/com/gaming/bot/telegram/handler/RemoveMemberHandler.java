package com.gaming.bot.telegram.handler;

import com.gaming.bot.service.GroupService;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveMemberHandler implements CommandHandler {

    private final GroupService groupService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/removemember");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID участника: /removemember 2");
            return;
        }

        try {
            Long memberId = Long.parseLong(parts[1].trim());

            groupService.removeMemberFromGroup(memberId);

            String response = """
                    ✅ Участник удален из группы!
                    
                    Участник больше не отображается в списке группы.
                    """;

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupsMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID участника");
        } catch (Exception e) {
            log.error("Error removing member", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении участника: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/removemember";
    }
}
