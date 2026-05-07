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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyGroupsHandler implements CommandHandler {

    private final UserService userService;
    private final GroupService groupService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/mygroups");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();

        try {
            User user = userService.getUserByTelegramId(telegramId);
            List<Group> groups = groupService.getUserGroups(user);

            if (groups.isEmpty()) {
                bot.sendMessageWithKeyboard(chatId, 
                        "👥 У вас пока нет групп\n\nСоздайте свою группу или присоединитесь по коду приглашения:",
                        KeyboardFactory.getGroupsMenuKeyboard());
                return;
            }

            String response = String.format("""
                    👥 Ваши группы (%d)
                    
                    Выберите группу для просмотра подробной информации:
                    """, groups.size());

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupsListKeyboard(groups));
        } catch (Exception e) {
            log.error("Error getting groups", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении списка групп: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/mygroups";
    }
}
