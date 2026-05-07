package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Group;
import com.gaming.bot.model.GroupMember;
import com.gaming.bot.service.*;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler implements CommandHandler {
    
    private final MyCharactersHandler myCharactersHandler;
    private final MyGroupsHandler myGroupsHandler;
    private final UserService userService;
    private final CharacterService characterService;
    private final GroupService groupService;
    private final EquipmentService equipmentService;
    private final WishlistService wishlistService;
    private final UserStateService userStateService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasCallbackQuery();
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        try {
            bot.answerCallbackQuery(update.getCallbackQuery().getId());

            if (callbackData.startsWith("view_char_")) {
                Long characterId = Long.parseLong(callbackData.replace("view_char_", ""));
                showCharacterDetails(update, bot, chatId, messageId, characterId);
                return;
            }
            
            if (callbackData.startsWith("view_equipment_for_")) {
                Long characterId = Long.parseLong(callbackData.replace("view_equipment_for_", ""));
                showEquipmentForCharacter(update, bot, chatId, characterId);
                return;
            }
            
            if (callbackData.startsWith("view_wishes_for_")) {
                Long characterId = Long.parseLong(callbackData.replace("view_wishes_for_", ""));
                showWishesForCharacter(update, bot, chatId, characterId);
                return;
            }
            
            if (callbackData.startsWith("add_equipment_for_")) {
                Long characterId = Long.parseLong(callbackData.replace("add_equipment_for_", ""));
                startAddingEquipment(chatId, characterId, bot);
                return;
            }
            
            if (callbackData.startsWith("add_wish_for_")) {
                Long characterId = Long.parseLong(callbackData.replace("add_wish_for_", ""));
                startAddingWish(chatId, characterId, bot);
                return;
            }
            
            if (callbackData.startsWith("edit_character_for_")) {
                Long characterId = Long.parseLong(callbackData.replace("edit_character_for_", ""));
                startEditingCharacter(chatId, characterId, bot);
                return;
            }
            
            if (callbackData.startsWith("delete_character_")) {
                Long characterId = Long.parseLong(callbackData.replace("delete_character_", ""));
                showDeleteCharacterConfirm(bot, chatId, characterId);
                return;
            }
            
            if (callbackData.startsWith("confirm_delete_character_")) {
                Long characterId = Long.parseLong(callbackData.replace("confirm_delete_character_", ""));
                deleteCharacter(update, bot, chatId, characterId);
                return;
            }
            
            // Навигация по желаниям
            if (callbackData.startsWith("wish_nav_")) {
                String[] parts = callbackData.replace("wish_nav_", "").split("_");
                Long characterId = Long.parseLong(parts[0]);
                int wishIndex = Integer.parseInt(parts[1]);
                showWishAtIndex(update, bot, chatId, characterId, wishIndex);
                return;
            }
            
            // Отметить желание выполненным
            if (callbackData.startsWith("fulfill_wish_")) {
                String[] parts = callbackData.replace("fulfill_wish_", "").split("_");
                Long wishId = Long.parseLong(parts[0]);
                Long characterId = Long.parseLong(parts[1]);
                fulfillWish(bot, chatId, wishId, characterId);
                return;
            }
            
            // Редактирование желания
            if (callbackData.startsWith("edit_wish_")) {
                String[] parts = callbackData.replace("edit_wish_", "").split("_");
                Long wishId = Long.parseLong(parts[0]);
                Long characterId = Long.parseLong(parts[1]);
                startEditingWish(chatId, wishId, characterId, bot);
                return;
            }
            
            // Удаление желания
            if (callbackData.startsWith("delete_wish_")) {
                String[] parts = callbackData.replace("delete_wish_", "").split("_");
                Long wishId = Long.parseLong(parts[0]);
                Long characterId = Long.parseLong(parts[1]);
                showDeleteWishConfirm(bot, chatId, wishId, characterId);
                return;
            }
            
            if (callbackData.startsWith("confirm_delete_wish_")) {
                String[] parts = callbackData.replace("confirm_delete_wish_", "").split("_");
                Long wishId = Long.parseLong(parts[0]);
                Long characterId = Long.parseLong(parts[1]);
                deleteWish(bot, chatId, wishId, characterId);
                return;
            }
            
            if (callbackData.startsWith("view_group_")) {
                Long groupId = Long.parseLong(callbackData.replace("view_group_", ""));
                showGroupDetails(update, bot, chatId, messageId, groupId);
                return;
            }
            
            if (callbackData.startsWith("group_members_")) {
                String[] parts = callbackData.replace("group_members_", "").split("_");
                Long groupId = Long.parseLong(parts[0]);
                int memberIndex = Integer.parseInt(parts[1]);
                showGroupMember(update, bot, chatId, groupId, memberIndex);
                return;
            }
            
            if (callbackData.startsWith("group_all_members_")) {
                Long groupId = Long.parseLong(callbackData.replace("group_all_members_", ""));
                showAllGroupMembers(update, bot, chatId, groupId);
                return;
            }
            
            if (callbackData.startsWith("remove_member_")) {
                String[] parts = callbackData.replace("remove_member_", "").split("_");
                Long groupId = Long.parseLong(parts[0]);
                Long memberId = Long.parseLong(parts[1]);
                removeMemberFromGroup(update, bot, chatId, groupId, memberId);
                return;
            }
            
            if (callbackData.startsWith("add_member_for_")) {
                Long groupId = Long.parseLong(callbackData.replace("add_member_for_", ""));
                startAddingMember(chatId, groupId, bot);
                return;
            }
            
            if (callbackData.startsWith("transfer_leadership_for_")) {
                Long groupId = Long.parseLong(callbackData.replace("transfer_leadership_for_", ""));
                startTransferLeadership(chatId, groupId, bot);
                return;
            }
            
            if (callbackData.startsWith("confirm_transfer_")) {
                String[] parts = callbackData.replace("confirm_transfer_", "").split("_");
                Long groupId = Long.parseLong(parts[0]);
                Long memberId = Long.parseLong(parts[1]);
                transferLeadership(update, bot, chatId, groupId, memberId);
                return;
            }
            
            if (callbackData.startsWith("delete_group_")) {
                Long groupId = Long.parseLong(callbackData.replace("delete_group_", ""));
                showDeleteGroupConfirm(bot, chatId, groupId);
                return;
            }
            
            if (callbackData.startsWith("confirm_delete_group_")) {
                Long groupId = Long.parseLong(callbackData.replace("confirm_delete_group_", ""));
                deleteGroup(update, bot, chatId, groupId);
                return;
            }
            
            if (callbackData.equals("join_group_prompt")) {
                startJoiningGroup(chatId, bot);
                return;
            }
            
            if (callbackData.startsWith("join_with_character_")) {
                String[] parts = callbackData.replace("join_with_character_", "").split("_");
                String invitationCode = parts[0];
                Long characterId = Long.parseLong(parts[1]);
                joinGroupWithCharacter(update, bot, chatId, invitationCode, characterId);
                return;
            }
            
            if (callbackData.startsWith("leave_group_")) {
                Long groupId = Long.parseLong(callbackData.replace("leave_group_", ""));
                showLeaveGroupConfirm(bot, chatId, groupId);
                return;
            }
            
            if (callbackData.startsWith("confirm_leave_group_")) {
                Long groupId = Long.parseLong(callbackData.replace("confirm_leave_group_", ""));
                leaveGroup(update, bot, chatId, groupId);
                return;
            }
            
            if (callbackData.equals("noop")) {
                return;
            }
            
            if (callbackData.equals("cancel_operation")) {
                cancelOperation(chatId, bot);
                return;
            }

            switch (callbackData) {
                case "main_menu":
                    showMainMenu(bot, chatId, messageId);
                    break;
                case "menu_characters":
                    showCharactersMenu(bot, chatId, messageId);
                    break;
                case "menu_equipment":
                    showEquipmentMenu(bot, chatId, messageId);
                    break;
                case "menu_wishes":
                    showWishesMenu(bot, chatId, messageId);
                    break;
                case "menu_groups":
                    showGroupsMenu(bot, chatId, messageId);
                    break;
                case "menu_help":
                    showHelp(bot, chatId, messageId);
                    break;
                case "create_character":
                    startCreatingCharacter(chatId, bot);
                    break;
                case "my_characters":
                    executeCommand(update, bot, "/mycharacters");
                    break;
                case "edit_character_prompt":
                    showEditCharacterPrompt(bot, chatId, messageId);
                    break;
                case "delete_character_prompt":
                    showDeleteCharacterPrompt(bot, chatId, messageId);
                    break;
                case "view_equipment_from_char":
                    showViewEquipmentPrompt(bot, chatId, messageId);
                    break;
                case "add_equipment_prompt":
                    showAddEquipmentPrompt(bot, chatId, messageId);
                    break;
                case "view_equipment_prompt":
                    showViewEquipmentPrompt(bot, chatId, messageId);
                    break;
                case "delete_equipment_prompt":
                    showDeleteEquipmentPrompt(bot, chatId, messageId);
                    break;
                case "add_wish_prompt":
                    showAddWishPrompt(bot, chatId, messageId);
                    break;
                case "view_wishes_prompt":
                    showViewWishesPrompt(bot, chatId, messageId);
                    break;
                case "fulfill_wish_prompt":
                    showFulfillWishPrompt(bot, chatId, messageId);
                    break;
                case "delete_wish_prompt":
                    showDeleteWishPrompt(bot, chatId, messageId);
                    break;
                case "create_group_prompt":
                    startCreatingGroup(chatId, bot);
                    break;
                case "my_groups":
                    executeCommand(update, bot, "/mygroups");
                    break;
                case "group_info_prompt":
                    showGroupInfoPrompt(bot, chatId, messageId);
                    break;
                case "add_member_prompt":
                    showAddMemberPrompt(bot, chatId, messageId);
                    break;
                case "remove_member_prompt":
                    showRemoveMemberPrompt(bot, chatId, messageId);
                    break;
                case "transfer_leadership_prompt":
                    showTransferLeadershipPrompt(bot, chatId, messageId);
                    break;
                case "delete_group_prompt":
                    showDeleteGroupPrompt(bot, chatId, messageId);
                    break;
                default:
                    log.warn("Unknown callback data: {}", callbackData);
            }
        } catch (Exception e) {
            log.error("Error handling callback query", e);
            bot.sendMessage(chatId, "❌ Произошла ошибка при обработке запроса");
        }
    }

    private void showMainMenu(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                🎮 Главное меню
                
                Выберите раздел:
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getMainMenuKeyboard());
    }

    private void showCharactersMenu(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                👤 Управление персонажами
                
                Здесь вы можете создавать и управлять своими персонажами.
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getCharactersMenuKeyboard());
    }

    private void showEquipmentMenu(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ⚔️ Управление экипировкой
                
                Добавляйте предметы к персонажам и просматривайте экипировку.
                Можно прикреплять фото предметов!
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getEquipmentMenuKeyboard());
    }

    private void showWishesMenu(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ⭐ Управление желаниями
                
                Создавайте список желаемых предметов для ваших персонажей.
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getWishesMenuKeyboard());
    }

    private void showGroupsMenu(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                👥 Управление группами
                
                Создавайте группы и приглашайте других игроков.
                Лидер группы видит экипировку и желания всех участников.
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getGroupsMenuKeyboard());
    }

    private void showHelp(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ℹ️ Справка
                
                📖 Как использовать бота:
                
                1. Создайте персонажа
                2. Добавьте экипировку и желания
                3. Создайте группу или присоединитесь к существующей
                4. Лидер группы может видеть всё снаряжение команды
                
                💡 Совет: Большинство команд работают через кнопки!
                Но текстовые команды тоже доступны - отправьте /help для списка.
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showAddEquipmentPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ➕ Добавление экипировки
                
                📝 Текстовый вариант:
                /addequipment ID_персонажа|название|тип|количество|описание
                
                Пример:
                /addequipment 1|Меч Андуил|Оружие|1|Легендарный меч
                
                📸 С изображением:
                1. Прикрепите фото предмета
                2. В подписи укажите команду
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showViewEquipmentPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                📋 Просмотр экипировки
                
                Формат: /myequipment ID_персонажа
                
                Пример: /myequipment 1
                
                Используйте кнопку "Мои персонажи" чтобы узнать ID
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showAddWishPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ➕ Добавление желания
                
                Формат: /addwish ID_персонажа|название|тип|приоритет|заметки
                
                Пример:
                /addwish 1|Щит Элендила|Щит|10|Хочу получить
                
                Приоритет от 1 до 10 (10 - самый высокий)
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showViewWishesPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                📋 Просмотр желаний
                
                Формат: /mywishes ID_персонажа
                
                Пример: /mywishes 1
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showGroupInfoPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                📋 Информация о группе
                
                Формат: /groupinfo ID_группы
                
                Пример: /groupinfo 1
                
                Используйте кнопку "Мои группы" чтобы узнать ID
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showAddMemberPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ➕ Добавление участника
                
                Формат: /addmember ID_группы|ID_персонажа
                
                Пример: /addmember 1|5
                
                💡 Попросите игрока узнать ID персонажа через /mycharacters
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showEditCharacterPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ✏️ Редактирование персонажа
                
                Формат: /editcharacter ID|имя|класс|уровень|описание
                
                Пример: /editcharacter 1|Арагорн|Воин|20|Король Гондора
                
                Используйте /mycharacters чтобы узнать ID персонажа
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showDeleteCharacterPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                🗑️ Удаление персонажа
                
                Формат: /deletecharacter ID
                
                Пример: /deletecharacter 1
                
                ⚠️ Это действие нельзя отменить!
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showDeleteEquipmentPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                🗑️ Удаление предмета
                
                Формат: /deleteequipment ID_предмета
                
                Пример: /deleteequipment 5
                
                Используйте /myequipment ID_персонажа для получения ID предметов
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showFulfillWishPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                ✅ Отметить желание выполненным
                
                Формат: /fulfillwish ID_желания
                
                Пример: /fulfillwish 3
                
                Используйте /mywishes ID_персонажа для получения ID желаний
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showDeleteWishPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                🗑️ Удаление желания
                
                Формат: /deletewish ID_желания
                
                Пример: /deletewish 3
                
                Используйте /mywishes ID_персонажа для получения ID желаний
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showRemoveMemberPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                🗑️ Удаление участника из группы
                
                Формат: /removemember ID_участника
                
                Пример: /removemember 2
                
                Используйте /groupinfo ID_группы для получения ID участников
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showTransferLeadershipPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                👑 Передача лидерства
                
                Формат: /transferleader ID_группы|ID_участника
                
                Пример: /transferleader 1|2
                
                ⚠️ После передачи вы перестанете быть лидером группы!
                Используйте /groupinfo для получения ID участников
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showDeleteGroupPrompt(CharacterGroupBot bot, Long chatId, Integer messageId) {
        String text = """
                🗑️ Удаление группы
                
                Формат: /deletegroup ID_группы
                
                Пример: /deletegroup 1
                
                ⚠️ Это действие нельзя отменить!
                Все участники будут исключены из группы.
                
                Отправьте команду в чат ⬇️
                """;
        editMessage(bot, chatId, messageId, text, KeyboardFactory.getBackToMainMenuKeyboard());
    }

    private void showCharacterDetails(Update update, CharacterGroupBot bot, Long chatId, Integer messageId, Long characterId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            Character character = characterService.getCharacterById(characterId, user);

            String text = String.format("""
                    👤 Персонаж: %s
                    
                    🆔 ID: %d
                    ⚔️ Класс: %s
                    📊 Уровень: %d
                    📝 Описание: %s
                    
                    📅 Создан: %s
                    📅 Обновлен: %s
                    
                    💡 Сообщите ID персонажа (%d) лидеру группы для приглашения
                    """,
                    character.getName(),
                    character.getId(),
                    character.getCharacterClass() != null ? character.getCharacterClass() : "Не указан",
                    character.getLevel(),
                    character.getDescription() != null ? character.getDescription() : "Нет описания",
                    character.getCreatedAt().toLocalDate(),
                    character.getUpdatedAt().toLocalDate(),
                    character.getId()
            );

            editMessage(bot, chatId, messageId, text, KeyboardFactory.getCharacterDetailsKeyboard(characterId));
        } catch (Exception e) {
            log.error("Error showing character details", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении информации о персонаже");
        }
    }

    private void showEquipmentForCharacter(Update update, CharacterGroupBot bot, Long chatId, Long characterId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            Character character = characterService.getCharacterById(characterId, user);
            
            List<com.gaming.bot.model.Equipment> equipmentList = equipmentService.getCharacterEquipment(character);

            if (equipmentList.isEmpty()) {
                bot.sendMessageWithKeyboard(chatId, 
                        String.format("⚔️ У персонажа %s пока нет экипировки", character.getName()),
                        KeyboardFactory.getCharacterDetailsKeyboard(characterId));
                return;
            }

            bot.sendMessage(chatId, String.format("⚔️ Экипировка персонажа %s:\n", character.getName()));
            
            for (int i = 0; i < equipmentList.size(); i++) {
                com.gaming.bot.model.Equipment equipment = equipmentList.get(i);
                boolean isLast = (i == equipmentList.size() - 1);
                
                String itemInfo = String.format("""
                        🆔 ID: %d
                        ⚔️ Предмет: %s
                        📦 Тип: %s
                        🔢 Количество: %d
                        📝 Описание: %s
                        """,
                        equipment.getId(),
                        equipment.getItemName(),
                        equipment.getItemType() != null ? equipment.getItemType() : "Не указан",
                        equipment.getQuantity(),
                        equipment.getDescription() != null ? equipment.getDescription() : "Нет описания"
                );

                if (equipment.getImageFileId() != null && !equipment.getImageFileId().isEmpty()) {
                    bot.sendPhoto(chatId, equipment.getImageFileId(), itemInfo);
                    if (isLast) {
                        bot.sendMessageWithKeyboard(chatId, "🔙 Назад к персонажу", 
                                KeyboardFactory.getCharacterDetailsKeyboard(characterId));
                    }
                } else {
                    if (isLast) {
                        bot.sendMessageWithKeyboard(chatId, itemInfo, KeyboardFactory.getCharacterDetailsKeyboard(characterId));
                    } else {
                        bot.sendMessage(chatId, itemInfo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error showing equipment", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении экипировки: " + e.getMessage());
        }
    }

    private void showWishesForCharacter(Update update, CharacterGroupBot bot, Long chatId, Long characterId) {
        showWishAtIndex(update, bot, chatId, characterId, 0);
    }
    
    private void showWishAtIndex(Update update, CharacterGroupBot bot, Long chatId, Long characterId, int index) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            Character character = characterService.getCharacterById(characterId, user);
            
            List<com.gaming.bot.model.Wishlist> wishes = wishlistService.getCharacterWishes(character);

            if (wishes.isEmpty()) {
                bot.sendMessageWithKeyboard(chatId, 
                        String.format("⭐ У персонажа %s пока нет желаний\n\nДобавьте желание через карточку персонажа.", character.getName()),
                        KeyboardFactory.getCharacterDetailsKeyboard(characterId));
                return;
            }

            if (index < 0 || index >= wishes.size()) {
                index = 0;
            }

            com.gaming.bot.model.Wishlist wish = wishes.get(index);
            
            String response = String.format("""
                    ⭐ Желания персонажа %s
                    
                    🆔 ID: %d
                    ⭐ Предмет: %s
                    📦 Тип: %s
                    🔥 Приоритет: %d
                    📝 Заметки: %s
                    %s
                    """,
                    character.getName(),
                    wish.getId(),
                    wish.getItemName(),
                    wish.getItemType() != null ? wish.getItemType() : "Не указан",
                    wish.getPriority(),
                    wish.getNotes() != null ? wish.getNotes() : "Нет заметок",
                    wish.getIsFulfilled() ? "✅ Получено" : "⏳ В ожидании"
            );

            bot.sendMessageWithKeyboard(chatId, response, 
                    KeyboardFactory.getWishNavigationKeyboard(characterId, index, wishes.size(), wish.getId()));
        } catch (Exception e) {
            log.error("Error showing wishes", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении желаний: " + e.getMessage());
        }
    }

    private void showGroupMember(Update update, CharacterGroupBot bot, Long chatId, Long groupId, int memberIndex) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            Group group = groupService.getGroupById(groupId, user);
            boolean isLeader = groupService.isUserLeader(group, user);
            
            List<GroupMember> members = groupService.getGroupMembers(group);

            if (members.isEmpty()) {
                bot.sendMessageWithKeyboard(chatId, 
                        String.format("👥 Группа %s пока пуста. Добавьте участников.", group.getName()),
                        KeyboardFactory.getGroupDetailsKeyboard(groupId, isLeader));
                return;
            }

            if (memberIndex < 0 || memberIndex >= members.size()) {
                memberIndex = 0;
            }

            GroupMember member = members.get(memberIndex);
            Character character = member.getCharacter();
            List<com.gaming.bot.model.Equipment> equipment = equipmentService.getCharacterEquipment(character);
            List<com.gaming.bot.model.Wishlist> wishes = wishlistService.getCharacterWishes(character);

            StringBuilder response = new StringBuilder(String.format("""
                    👥 Группа: %s
                    
                    ━━━━━━━━━━━━━━━
                    🆔 ID участника: %d
                    👤 Персонаж: %s (Lvl %d %s)
                    👥 Игрок: %s
                    
                    ⚔️ Экипировка (%d предметов):
                    """,
                    group.getName(),
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
                for (com.gaming.bot.model.Equipment eq : equipment) {
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
                for (com.gaming.bot.model.Wishlist wish : wishes) {
                    if (!wish.getIsFulfilled()) {
                        response.append(String.format("   • %s (%s) [Приоритет: %d]\n", 
                                wish.getItemName(),
                                wish.getItemType() != null ? wish.getItemType() : "?",
                                wish.getPriority()));
                    }
                }
            }

            bot.sendMessageWithKeyboard(chatId, response.toString(), 
                    KeyboardFactory.getGroupMemberNavigationKeyboard(groupId, memberIndex, members.size(), member.getId(), isLeader));
        } catch (Exception e) {
            log.error("Error showing group member", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении информации об участнике: " + e.getMessage());
        }
    }

    private void showAllGroupMembers(Update update, CharacterGroupBot bot, Long chatId, Long groupId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            Group group = groupService.getGroupById(groupId, user);
            
            List<GroupMember> members = groupService.getGroupMembers(group);

            StringBuilder response = new StringBuilder(String.format("""
                    👥 Группа: %s
                    📝 Описание: %s
                    👑 Лидер: %s
                    
                    👤 Все участники:
                    
                    """,
                    group.getName(),
                    group.getDescription() != null ? group.getDescription() : "Нет описания",
                    group.getLeader().getUsername()
            ));

            if (members.isEmpty()) {
                response.append("Группа пока пуста. Добавьте участников.");
            } else {
                for (GroupMember member : members) {
                    Character character = member.getCharacter();
                    List<com.gaming.bot.model.Equipment> equipment = equipmentService.getCharacterEquipment(character);
                    List<com.gaming.bot.model.Wishlist> wishes = wishlistService.getCharacterWishes(character);
                    
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
                        for (com.gaming.bot.model.Equipment eq : equipment) {
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
                        for (com.gaming.bot.model.Wishlist wish : wishes) {
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
                                KeyboardFactory.getGroupAllMembersKeyboard(groupId));
                    } else {
                        bot.sendMessage(chatId, fullResponse.substring(start, end));
                    }
                }
            } else {
                bot.sendMessageWithKeyboard(chatId, response.toString(), 
                        KeyboardFactory.getGroupAllMembersKeyboard(groupId));
            }
        } catch (Exception e) {
            log.error("Error showing all group members", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении информации о группе: " + e.getMessage());
        }
    }

    private void removeMemberFromGroup(Update update, CharacterGroupBot bot, Long chatId, Long groupId, Long memberId) {
        try {
            groupService.removeMemberFromGroup(memberId);
            
            bot.sendMessageWithKeyboard(chatId, 
                    "✅ Участник удален из группы!", 
                    KeyboardFactory.getGroupDetailsKeyboard(groupId));
        } catch (Exception e) {
            log.error("Error removing member from group", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении участника: " + e.getMessage());
        }
    }

    private void showDeleteGroupConfirm(CharacterGroupBot bot, Long chatId, Long groupId) {
        String text = """
                ⚠️ Удаление группы
                
                Вы уверены, что хотите удалить эту группу?
                Все участники будут исключены.
                
                Это действие нельзя отменить!
                """;
        bot.sendMessageWithKeyboard(chatId, text, KeyboardFactory.getDeleteGroupConfirmKeyboard(groupId));
    }

    private void deleteGroup(Update update, CharacterGroupBot bot, Long chatId, Long groupId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            
            groupService.deleteGroup(groupId, user);
            
            bot.sendMessageWithKeyboard(chatId, 
                    "✅ Группа удалена!", 
                    KeyboardFactory.getGroupsMenuKeyboard());
        } catch (Exception e) {
            log.error("Error deleting group", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении группы: " + e.getMessage());
        }
    }

    private void showLeaveGroupConfirm(CharacterGroupBot bot, Long chatId, Long groupId) {
        String text = """
                ⚠️ Выход из группы
                
                Вы уверены, что хотите покинуть эту группу?
                Все ваши персонажи будут исключены из группы.
                """;
        bot.sendMessageWithKeyboard(chatId, text, KeyboardFactory.getLeaveGroupConfirmKeyboard(groupId));
    }

    private void leaveGroup(Update update, CharacterGroupBot bot, Long chatId, Long groupId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            
            groupService.leaveGroup(groupId, user);
            
            bot.sendMessageWithKeyboard(chatId, 
                    "✅ Вы покинули группу!", 
                    KeyboardFactory.getGroupsMenuKeyboard());
        } catch (Exception e) {
            log.error("Error leaving group", e);
            bot.sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    private void showGroupDetails(Update update, CharacterGroupBot bot, Long chatId, Integer messageId, Long groupId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            Group group = groupService.getGroupById(groupId, user);
            boolean isLeader = groupService.isUserLeader(group, user);
            
            List<GroupMember> members = groupService.getGroupMembers(group);

            String leaderBadge = isLeader ? " (вы лидер)" : "";
            String text = String.format("""
                    👥 Группа: %s%s
                    
                    🆔 ID: %d
                    📝 Описание: %s
                    👑 Лидер: %s
                    
                    👥 Участников: %d
                    📅 Создана: %s
                    
                    💡 Нажмите "Участники и экипировка" для полного просмотра
                    """,
                    group.getName(),
                    leaderBadge,
                    group.getId(),
                    group.getDescription() != null ? group.getDescription() : "Нет описания",
                    group.getLeader().getUsername(),
                    members.size(),
                    group.getCreatedAt().toLocalDate()
            );

            editMessage(bot, chatId, messageId, text, KeyboardFactory.getGroupDetailsKeyboard(groupId, isLeader));
        } catch (Exception e) {
            log.error("Error showing group details", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении информации о группе");
        }
    }

    private void editMessage(CharacterGroupBot bot, Long chatId, Integer messageId, 
                           String text, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        
        try {
            bot.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message", e);
        }
    }

    private void executeCommand(Update originalUpdate, CharacterGroupBot bot, String commandText) {
        CallbackQuery callbackQuery = originalUpdate.getCallbackQuery();
        org.telegram.telegrambots.meta.api.objects.User telegramUser = callbackQuery.getFrom();
        Long chatId = callbackQuery.getMessage().getChatId();
        
        Update fakeUpdate = new Update();
        Message fakeMessage = new Message();
        fakeMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        
        Chat chat = new Chat();
        chat.setId(chatId);
        fakeMessage.setChat(chat);
        
        fakeMessage.setFrom(telegramUser);
        fakeMessage.setText(commandText);
        fakeUpdate.setMessage(fakeMessage);
        
        switch (commandText) {
            case "/mycharacters":
                myCharactersHandler.handle(fakeUpdate, bot);
                break;
            case "/mygroups":
                myGroupsHandler.handle(fakeUpdate, bot);
                break;
            default:
                log.warn("Unknown command: {}", commandText);
        }
    }

    // === Интерактивный ввод ===
    
    private void startAddingEquipment(Long chatId, Long characterId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setData(chatId, "character_id", characterId);
        userStateService.setState(chatId, UserStateService.State.ADDING_EQUIPMENT_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Добавление экипировки\n\n⚔️ Введите название предмета:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    private void startAddingWish(Long chatId, Long characterId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setData(chatId, "character_id", characterId);
        userStateService.setState(chatId, UserStateService.State.ADDING_WISH_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Добавление желания\n\n⭐ Введите название желаемого предмета:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    private void startCreatingCharacter(Long chatId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setState(chatId, UserStateService.State.CREATING_CHARACTER_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Создание персонажа\n\n👤 Введите имя персонажа:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    private void startCreatingGroup(Long chatId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setState(chatId, UserStateService.State.CREATING_GROUP_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Создание группы\n\n👥 Введите название группы:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    private void startAddingMember(Long chatId, Long groupId, CharacterGroupBot bot) {
        try {
            Long telegramId = chatId;
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            
            var invitation = groupService.createInvitation(groupId, user);
            
            String response = String.format("""
                    🔗 Код приглашения в группу
                    
                    📋 Код: %s
                    
                    ⏱️ Действует 15 минут
                    
                    💡 Отправьте этот код игроку. 
                    Он должен ввести команду:
                    /join %s
                    
                    Или использовать меню "Присоединиться к группе"
                    """,
                    invitation.getCode(),
                    invitation.getCode()
            );
            
            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupDetailsKeyboard(groupId, true));
        } catch (Exception e) {
            log.error("Error creating invitation", e);
            bot.sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }
    
    private void startJoiningGroup(Long chatId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setState(chatId, UserStateService.State.JOINING_GROUP_CODE);
        bot.sendMessageWithKeyboard(chatId, 
                "🔗 Присоединение к группе\n\n📋 Введите код приглашения:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    private void joinGroupWithCharacter(Update update, CharacterGroupBot bot, Long chatId, String invitationCode, Long characterId) {
        try {
            Long telegramId = chatId;
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            com.gaming.bot.model.Character character = characterService.getCharacterById(characterId, user);
            
            GroupMember member = groupService.useInvitation(invitationCode, character);
            
            String response = String.format("""
                    ✅ Вы присоединились к группе!
                    
                    👥 Группа: %s
                    👤 Персонаж: %s
                    """,
                    member.getGroup().getName(),
                    character.getName()
            );
            
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(chatId.toString());
            editMessage.setMessageId(messageId);
            editMessage.setText(response);
            editMessage.setReplyMarkup(KeyboardFactory.getGroupDetailsKeyboard(member.getGroup().getId(), false));
            bot.executeMessage(editMessage);
        } catch (Exception e) {
            log.error("Error joining group", e);
            bot.sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }
    
    private void startEditingCharacter(Long chatId, Long characterId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setData(chatId, "character_id", characterId);
        userStateService.setState(chatId, UserStateService.State.EDITING_CHARACTER_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "✏️ Редактирование персонажа\n\n👤 Введите новое имя:\n\nИли отправьте '-' чтобы оставить текущее",
                KeyboardFactory.getCancelKeyboard());
    }
    
    private void showDeleteCharacterConfirm(CharacterGroupBot bot, Long chatId, Long characterId) {
        String text = """
                ⚠️ Удаление персонажа
                
                Вы уверены, что хотите удалить этого персонажа?
                Вся экипировка и желания будут удалены.
                
                Это действие нельзя отменить!
                """;
        bot.sendMessageWithKeyboard(chatId, text, KeyboardFactory.getDeleteCharacterConfirmKeyboard(characterId));
    }
    
    private void deleteCharacter(Update update, CharacterGroupBot bot, Long chatId, Long characterId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            
            characterService.deleteCharacter(characterId, user);
            
            bot.sendMessageWithKeyboard(chatId, 
                    "✅ Персонаж удален!", 
                    KeyboardFactory.getCharactersMenuKeyboard());
        } catch (Exception e) {
            log.error("Error deleting character", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении персонажа: " + e.getMessage());
        }
    }
    
    // === Работа с желаниями ===
    
    private void fulfillWish(CharacterGroupBot bot, Long chatId, Long wishId, Long characterId) {
        try {
            wishlistService.fulfillWish(wishId);
            bot.sendMessageWithKeyboard(chatId, 
                    "✅ Желание отмечено как выполненное!", 
                    KeyboardFactory.getCharacterDetailsKeyboard(characterId));
        } catch (Exception e) {
            log.error("Error fulfilling wish", e);
            bot.sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }
    
    private void startEditingWish(Long chatId, Long wishId, Long characterId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setData(chatId, "wish_id", wishId);
        userStateService.setData(chatId, "character_id", characterId);
        userStateService.setState(chatId, UserStateService.State.EDITING_WISH_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "✏️ Редактирование желания\n\n⭐ Введите новое название:\n\nИли отправьте '-' чтобы оставить текущее",
                KeyboardFactory.getCancelKeyboard());
    }
    
    private void showDeleteWishConfirm(CharacterGroupBot bot, Long chatId, Long wishId, Long characterId) {
        String text = """
                ⚠️ Удаление желания
                
                Вы уверены, что хотите удалить это желание?
                """;
        bot.sendMessageWithKeyboard(chatId, text, KeyboardFactory.getDeleteWishConfirmKeyboard(wishId, characterId));
    }
    
    private void deleteWish(CharacterGroupBot bot, Long chatId, Long wishId, Long characterId) {
        try {
            wishlistService.deleteWish(wishId);
            bot.sendMessageWithKeyboard(chatId, 
                    "✅ Желание удалено!", 
                    KeyboardFactory.getCharacterDetailsKeyboard(characterId));
        } catch (Exception e) {
            log.error("Error deleting wish", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении желания: " + e.getMessage());
        }
    }
    
    // === Передача лидерства ===
    
    private void startTransferLeadership(Long chatId, Long groupId, CharacterGroupBot bot) {
        try {
            Long telegramId = chatId;
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            Group group = groupService.getGroupByIdForLeader(groupId, user);
            List<GroupMember> members = groupService.getGroupMembers(group);
            
            if (members.isEmpty()) {
                bot.sendMessage(chatId, "❌ В группе нет участников для передачи лидерства");
                return;
            }
            
            bot.sendMessageWithKeyboard(chatId, 
                    "👑 Выберите нового лидера группы:",
                    KeyboardFactory.getTransferLeadershipKeyboard(groupId, members));
        } catch (Exception e) {
            log.error("Error starting transfer leadership", e);
            bot.sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }
    
    private void transferLeadership(Update update, CharacterGroupBot bot, Long chatId, Long groupId, Long memberId) {
        try {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            com.gaming.bot.model.User user = userService.getUserByTelegramId(telegramId);
            
            groupService.transferLeadership(groupId, user, memberId);
            
            bot.sendMessageWithKeyboard(chatId, 
                    "✅ Лидерство передано!", 
                    KeyboardFactory.getGroupsMenuKeyboard());
        } catch (Exception e) {
            log.error("Error transferring leadership", e);
            bot.sendMessage(chatId, "❌ Ошибка при передаче лидерства: " + e.getMessage());
        }
    }
    
    private void cancelOperation(Long chatId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        bot.sendMessageWithKeyboard(chatId, "❌ Операция отменена", KeyboardFactory.getMainMenuKeyboard());
    }

    @Override
    public String getCommand() {
        return "callback_query";
    }
}
