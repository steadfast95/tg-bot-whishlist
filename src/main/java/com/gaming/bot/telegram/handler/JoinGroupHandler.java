package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.GroupInvitation;
import com.gaming.bot.model.User;
import com.gaming.bot.service.CharacterService;
import com.gaming.bot.service.GroupService;
import com.gaming.bot.service.UserService;
import com.gaming.bot.service.UserStateService;
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
public class JoinGroupHandler implements CommandHandler {

    private final UserService userService;
    private final GroupService groupService;
    private final CharacterService characterService;
    private final UserStateService userStateService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() &&
               update.getMessage().hasText() &&
               update.getMessage().getText().startsWith("/join");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();
        
        // Формат: /join CODE или /join
        String[] parts = text.split("\\s+", 2);
        
        if (parts.length < 2 || parts[1].isBlank()) {
            // Если код не передан - показываем промпт для ввода
            userStateService.clearState(chatId);
            userStateService.setState(chatId, UserStateService.State.JOINING_GROUP_CODE);
            bot.sendMessageWithKeyboard(chatId,
                    "🔗 Присоединение к группе\n\n📋 Введите код приглашения:",
                    KeyboardFactory.getCancelKeyboard());
            return;
        }
        
        String code = parts[1].trim().toUpperCase();
        
        try {
            GroupInvitation invitation = groupService.getValidInvitation(code);
            
            Long telegramId = update.getMessage().getFrom().getId();
            User user = userService.getUserByTelegramId(telegramId);
            List<Character> characters = characterService.getUserCharacters(user);
            
            if (characters.isEmpty()) {
                bot.sendMessageWithKeyboard(chatId,
                        "❌ У вас нет персонажей!\n\nСначала создайте персонажа в меню Персонажи.",
                        KeyboardFactory.getMainMenuKeyboard());
                return;
            }
            
            String response = String.format("""
                    ✅ Код приглашения принят!
                    
                    👥 Группа: %s
                    👤 Лидер: %s
                    
                    Выберите персонажа для вступления:
                    """,
                    invitation.getGroup().getName(),
                    invitation.getGroup().getLeader().getUsername()
            );
            
            bot.sendMessageWithKeyboard(chatId, response,
                    KeyboardFactory.getJoinGroupSelectCharacterKeyboard(code, characters));
        } catch (Exception e) {
            log.error("Error handling /join command", e);
            bot.sendMessage(chatId, "❌ " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/join";
    }
}
