package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.*;
import com.gaming.bot.model.Character;
import com.gaming.bot.service.*;
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
public class GroupInfoHandler implements CommandHandler {

    private final UserService userService;
    private final GroupService groupService;
    private final EquipmentService equipmentService;
    private final WishlistService wishlistService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/groupinfo");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID группы: /groupinfo 1\nИспользуйте /mygroups для получения списка");
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            Long groupId = Long.parseLong(parts[1].trim());
            Group group = groupService.getGroupById(groupId, user);
            
            List<GroupMember> members = groupService.getGroupMembers(group);

            StringBuilder response = new StringBuilder(String.format("""
                    👥 Группа: %s
                    📝 Описание: %s
                    👑 Лидер: %s
                    
                    👤 Участники:
                    
                    """,
                    group.getName(),
                    group.getDescription() != null ? group.getDescription() : "Нет описания",
                    group.getLeader().getUsername()
            ));

            if (members.isEmpty()) {
                response.append("Группа пока пуста. Добавьте участников командой /addmember");
            } else {
                for (GroupMember member : members) {
                    Character character = member.getCharacter();
                    List<Equipment> equipment = equipmentService.getCharacterEquipment(character);
                    List<Wishlist> wishes = wishlistService.getCharacterWishes(character);
                    
                    response.append(String.format("""
                            ━━━━━━━━━━━━━━━
                            🆔 ID участника: %d
                            👤 Персонаж: %s (Lvl %d %s)
                            👥 Игрок: %s
                            
                            ⚔️ Экипировка (%d предметов):
                            """,
                            member.getId(),
                            character.getName(),
                            character.getLevel(),
                            character.getCharacterClass() != null ? character.getCharacterClass() : "?",
                            character.getUser().getUsername(),
                            equipment.size()
                    ));
                    
                    if (equipment.isEmpty()) {
                        response.append("   Нет экипировки\n");
                    } else {
                        for (Equipment eq : equipment) {
                            String photoMark = eq.getImageFileId() != null ? "📸 " : "";
                            response.append(String.format("   • %s%s (%s) x%d\n", 
                                    photoMark,
                                    eq.getItemName(), 
                                    eq.getItemType() != null ? eq.getItemType() : "?",
                                    eq.getQuantity()));
                        }
                    }
                    
                    response.append(String.format("\n⭐ Желания (%d):\n", wishes.size()));
                    if (wishes.isEmpty()) {
                        response.append("   Нет желаний\n");
                    } else {
                        for (Wishlist wish : wishes) {
                            if (!wish.getIsFulfilled()) {
                                response.append(String.format("   • %s (%s) [Приоритет: %d]\n", 
                                        wish.getItemName(),
                                        wish.getItemType() != null ? wish.getItemType() : "?",
                                        wish.getPriority()));
                            }
                        }
                    }
                    response.append("\n");
                }
            }

            if (response.length() > 4000) {
                String fullResponse = response.toString();
                int chunks = (fullResponse.length() / 4000) + 1;
                for (int i = 0; i < chunks; i++) {
                    int start = i * 4000;
                    int end = Math.min(start + 4000, fullResponse.length());
                    if (i == chunks - 1) {
                        bot.sendMessageWithKeyboard(chatId, fullResponse.substring(start, end), 
                                KeyboardFactory.getGroupsMenuKeyboard());
                    } else {
                        bot.sendMessage(chatId, fullResponse.substring(start, end));
                    }
                }
            } else {
                bot.sendMessageWithKeyboard(chatId, response.toString(), 
                        KeyboardFactory.getGroupsMenuKeyboard());
            }
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID группы");
        } catch (Exception e) {
            log.error("Error getting group info", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении информации о группе: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/groupinfo";
    }
}
