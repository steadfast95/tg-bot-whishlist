package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.Group;
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
public class CreateGroupHandler implements CommandHandler {

    private final UserService userService;
    private final GroupService groupService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/creategroup");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split("\\|", 3);
        
        if (parts.length < 1 || parts[0].replace("/creategroup", "").trim().isEmpty()) {
            String usage = """
                    👥 Создание группы
                    
                    Формат: /creategroup название|описание
                    
                    Пример:
                    /creategroup Хранители Кольца|Братство для уничтожения Кольца Всевластия
                    
                    Минимум: /creategroup Хранители Кольца
                    """;
            bot.sendMessage(chatId, usage);
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            
            String name = parts[0].replace("/creategroup", "").trim();
            String description = parts.length > 1 ? parts[1].trim() : null;

            Group group = groupService.createGroup(user, name, description);

            String response = String.format("""
                    ✅ Группа создана!
                    
                    👥 Название: %s
                    📝 Описание: %s
                    👑 Лидер: %s
                    🆔 ID: %d
                    
                    Добавляйте участников командой /addmember
                    """,
                    group.getName(),
                    group.getDescription() != null ? group.getDescription() : "Нет описания",
                    user.getUsername(),
                    group.getId()
            );

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupsMenuKeyboard());
        } catch (Exception e) {
            log.error("Error creating group", e);
            bot.sendMessage(chatId, "❌ Ошибка при создании группы: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/creategroup";
    }
}
