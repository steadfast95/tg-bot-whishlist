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
public class TransferLeadershipHandler implements CommandHandler {

    private final UserService userService;
    private final GroupService groupService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/transferleader");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split("\\|", 3);
        
        if (parts.length < 2) {
            String usage = """
                    👑 Передача лидерства
                    
                    Формат: /transferleader ID_группы|ID_участника
                    
                    Пример: /transferleader 1|2
                    
                    ⚠️ После передачи вы перестанете быть лидером группы!
                    Используйте /groupinfo для получения ID участников
                    """;
            bot.sendMessage(chatId, usage);
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            
            Long groupId = Long.parseLong(parts[0].replace("/transferleader", "").trim());
            Long newLeaderId = Long.parseLong(parts[1].trim());

            groupService.transferLeadership(groupId, user, newLeaderId);

            String response = """
                    ✅ Лидерство успешно передано!
                    
                    Новый лидер группы теперь имеет все права управления.
                    Вы остаетесь участником группы.
                    """;

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupsMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID");
        } catch (Exception e) {
            log.error("Error transferring leadership", e);
            bot.sendMessage(chatId, "❌ Ошибка при передаче лидерства: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/transferleader";
    }
}
