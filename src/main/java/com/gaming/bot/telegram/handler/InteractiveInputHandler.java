package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Equipment;
import com.gaming.bot.model.Group;
import com.gaming.bot.model.User;
import com.gaming.bot.model.Wishlist;
import com.gaming.bot.service.*;
import com.gaming.bot.service.UserStateService.State;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class InteractiveInputHandler implements CommandHandler {

    private final UserStateService userStateService;
    private final UserService userService;
    private final CharacterService characterService;
    private final EquipmentService equipmentService;
    private final WishlistService wishlistService;
    private final GroupService groupService;

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        String text = update.getMessage().getText();
        if (text.startsWith("/")) {
            return false;
        }
        Long chatId = update.getMessage().getChatId();
        State state = userStateService.getState(chatId);
        return state != State.NONE;
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();
        State state = userStateService.getState(chatId);

        try {
            switch (state) {
                // Добавление экипировки
                case ADDING_EQUIPMENT_NAME -> handleEquipmentName(chatId, text, bot);
                case ADDING_EQUIPMENT_TYPE -> handleEquipmentType(chatId, text, bot);
                case ADDING_EQUIPMENT_QUANTITY -> handleEquipmentQuantity(chatId, text, bot);
                case ADDING_EQUIPMENT_DESCRIPTION -> handleEquipmentDescription(chatId, text, bot);
                
                // Добавление желания
                case ADDING_WISH_NAME -> handleWishName(chatId, text, bot);
                case ADDING_WISH_TYPE -> handleWishType(chatId, text, bot);
                case ADDING_WISH_PRIORITY -> handleWishPriority(chatId, text, bot);
                case ADDING_WISH_NOTES -> handleWishNotes(chatId, text, bot);
                
                // Создание персонажа
                case CREATING_CHARACTER_NAME -> handleCharacterName(chatId, text, bot);
                case CREATING_CHARACTER_CLASS -> handleCharacterClass(chatId, text, bot);
                case CREATING_CHARACTER_LEVEL -> handleCharacterLevel(chatId, text, bot);
                case CREATING_CHARACTER_DESCRIPTION -> handleCharacterDescription(chatId, text, bot);
                
                // Создание группы
                case CREATING_GROUP_NAME -> handleGroupName(chatId, text, bot);
                case CREATING_GROUP_DESCRIPTION -> handleGroupDescription(chatId, text, bot);
                
                // Добавление участника (устаревшее)
                case ADDING_MEMBER_CHARACTER_ID -> handleAddMemberCharacterId(chatId, text, bot);
                
                // Присоединение к группе по коду
                case JOINING_GROUP_CODE -> handleJoiningGroupCode(chatId, text, bot);
                
                // Редактирование персонажа
                case EDITING_CHARACTER_NAME -> handleEditCharacterName(chatId, text, bot);
                case EDITING_CHARACTER_CLASS -> handleEditCharacterClass(chatId, text, bot);
                case EDITING_CHARACTER_LEVEL -> handleEditCharacterLevel(chatId, text, bot);
                case EDITING_CHARACTER_DESCRIPTION -> handleEditCharacterDescription(chatId, text, bot);
                
                // Редактирование желания
                case EDITING_WISH_NAME -> handleEditWishName(chatId, text, bot);
                case EDITING_WISH_TYPE -> handleEditWishType(chatId, text, bot);
                case EDITING_WISH_PRIORITY -> handleEditWishPriority(chatId, text, bot);
                case EDITING_WISH_NOTES -> handleEditWishNotes(chatId, text, bot);
                
                // Ожидание файла импорта - просто игнорируем текст (ожидаем файл)
                case WAITING_FOR_IMPORT -> {
                    bot.sendMessage(chatId, "📁 Ожидаю JSON-файл бэкапа.\n\nДля отмены отправьте /cancel");
                }
                
                default -> {
                    userStateService.clearState(chatId);
                    bot.sendMessage(chatId, "Неизвестное состояние. Попробуйте снова.");
                }
            }
        } catch (Exception e) {
            log.error("Error in interactive input", e);
            userStateService.clearState(chatId);
            bot.sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    // === Добавление экипировки ===
    
    private void handleEquipmentName(Long chatId, String text, CharacterGroupBot bot) {
        userStateService.setData(chatId, "equipment_name", text);
        userStateService.setState(chatId, State.ADDING_EQUIPMENT_TYPE);
        bot.sendMessageWithKeyboard(chatId, 
                "📦 Введите тип предмета (например: Оружие, Броня, Аксессуар):\n\nИли отправьте '-' чтобы пропустить",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEquipmentType(Long chatId, String text, CharacterGroupBot bot) {
        String type = text.equals("-") ? null : text;
        userStateService.setData(chatId, "equipment_type", type);
        userStateService.setState(chatId, State.ADDING_EQUIPMENT_QUANTITY);
        bot.sendMessageWithKeyboard(chatId, 
                "🔢 Введите количество (число):\n\nИли отправьте '-' для значения 1",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEquipmentQuantity(Long chatId, String text, CharacterGroupBot bot) {
        int quantity = 1;
        if (!text.equals("-")) {
            try {
                quantity = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                bot.sendMessage(chatId, "❌ Введите число или '-'");
                return;
            }
        }
        userStateService.setData(chatId, "equipment_quantity", quantity);
        userStateService.setState(chatId, State.ADDING_EQUIPMENT_DESCRIPTION);
        bot.sendMessageWithKeyboard(chatId, 
                "📝 Введите описание предмета:\n\nИли отправьте '-' чтобы пропустить",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEquipmentDescription(Long chatId, String text, CharacterGroupBot bot) {
        String description = text.equals("-") ? null : text;
        
        Long characterId = userStateService.getDataAsLong(chatId, "character_id");
        String name = userStateService.getDataAsString(chatId, "equipment_name");
        String type = userStateService.getDataAsString(chatId, "equipment_type");
        Integer quantity = userStateService.getDataAsInt(chatId, "equipment_quantity");
        
        Long telegramId = chatId;
        User user = userService.getUserByTelegramId(telegramId);
        Character character = characterService.getCharacterById(characterId, user);
        
        Equipment equipment = equipmentService.addEquipment(character, name, type, quantity, description);
        
        userStateService.clearState(chatId);
        
        String response = String.format("""
                ✅ Экипировка добавлена!
                
                ⚔️ %s
                📦 Тип: %s
                🔢 Количество: %d
                📝 Описание: %s
                """,
                equipment.getItemName(),
                equipment.getItemType() != null ? equipment.getItemType() : "Не указан",
                equipment.getQuantity(),
                equipment.getDescription() != null ? equipment.getDescription() : "Нет"
        );
        
        bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharacterDetailsKeyboard(characterId));
    }

    // === Добавление желания ===
    
    private void handleWishName(Long chatId, String text, CharacterGroupBot bot) {
        userStateService.setData(chatId, "wish_name", text);
        userStateService.setState(chatId, State.ADDING_WISH_TYPE);
        bot.sendMessageWithKeyboard(chatId, 
                "📦 Введите тип предмета:\n\nИли отправьте '-' чтобы пропустить",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleWishType(Long chatId, String text, CharacterGroupBot bot) {
        String type = text.equals("-") ? null : text;
        userStateService.setData(chatId, "wish_type", type);
        userStateService.setState(chatId, State.ADDING_WISH_PRIORITY);
        bot.sendMessageWithKeyboard(chatId, 
                "⭐ Введите приоритет от 1 до 10 (10 - самый высокий):\n\nИли отправьте '-' для значения 5",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleWishPriority(Long chatId, String text, CharacterGroupBot bot) {
        int priority = 5;
        if (!text.equals("-")) {
            try {
                priority = Integer.parseInt(text);
                if (priority < 1 || priority > 10) {
                    bot.sendMessage(chatId, "❌ Приоритет должен быть от 1 до 10");
                    return;
                }
            } catch (NumberFormatException e) {
                bot.sendMessage(chatId, "❌ Введите число от 1 до 10 или '-'");
                return;
            }
        }
        userStateService.setData(chatId, "wish_priority", priority);
        userStateService.setState(chatId, State.ADDING_WISH_NOTES);
        bot.sendMessageWithKeyboard(chatId, 
                "📝 Введите заметки:\n\nИли отправьте '-' чтобы пропустить",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleWishNotes(Long chatId, String text, CharacterGroupBot bot) {
        String notes = text.equals("-") ? null : text;
        
        Long characterId = userStateService.getDataAsLong(chatId, "character_id");
        String name = userStateService.getDataAsString(chatId, "wish_name");
        String type = userStateService.getDataAsString(chatId, "wish_type");
        Integer priority = userStateService.getDataAsInt(chatId, "wish_priority");
        
        Long telegramId = chatId;
        User user = userService.getUserByTelegramId(telegramId);
        Character character = characterService.getCharacterById(characterId, user);
        
        Wishlist wish = wishlistService.addWish(character, name, type, priority, notes);
        
        userStateService.clearState(chatId);
        
        String response = String.format("""
                ✅ Желание добавлено!
                
                ⭐ %s
                📦 Тип: %s
                🔥 Приоритет: %d
                📝 Заметки: %s
                """,
                wish.getItemName(),
                wish.getItemType() != null ? wish.getItemType() : "Не указан",
                wish.getPriority(),
                wish.getNotes() != null ? wish.getNotes() : "Нет"
        );
        
        bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharacterDetailsKeyboard(characterId));
    }

    // === Создание персонажа ===
    
    private void handleCharacterName(Long chatId, String text, CharacterGroupBot bot) {
        userStateService.setData(chatId, "character_name", text);
        userStateService.setState(chatId, State.CREATING_CHARACTER_CLASS);
        bot.sendMessageWithKeyboard(chatId, 
                "⚔️ Введите класс персонажа (например: Воин, Маг, Лучник):\n\nИли отправьте '-' чтобы пропустить",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleCharacterClass(Long chatId, String text, CharacterGroupBot bot) {
        String charClass = text.equals("-") ? null : text;
        userStateService.setData(chatId, "character_class", charClass);
        userStateService.setState(chatId, State.CREATING_CHARACTER_LEVEL);
        bot.sendMessageWithKeyboard(chatId, 
                "📊 Введите уровень персонажа (число):\n\nИли отправьте '-' для уровня 1",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleCharacterLevel(Long chatId, String text, CharacterGroupBot bot) {
        int level = 1;
        if (!text.equals("-")) {
            try {
                level = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                bot.sendMessage(chatId, "❌ Введите число или '-'");
                return;
            }
        }
        userStateService.setData(chatId, "character_level", level);
        userStateService.setState(chatId, State.CREATING_CHARACTER_DESCRIPTION);
        bot.sendMessageWithKeyboard(chatId, 
                "📝 Введите описание персонажа:\n\nИли отправьте '-' чтобы пропустить",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleCharacterDescription(Long chatId, String text, CharacterGroupBot bot) {
        String description = text.equals("-") ? null : text;
        
        String name = userStateService.getDataAsString(chatId, "character_name");
        String charClass = userStateService.getDataAsString(chatId, "character_class");
        Integer level = userStateService.getDataAsInt(chatId, "character_level");
        
        Long telegramId = chatId;
        User user = userService.getUserByTelegramId(telegramId);
        
        Character character = characterService.createCharacter(user, name, charClass, level, description);
        
        userStateService.clearState(chatId);
        
        String response = String.format("""
                ✅ Персонаж создан!
                
                👤 %s
                ⚔️ Класс: %s
                📊 Уровень: %d
                📝 Описание: %s
                🆔 ID: %d
                """,
                character.getName(),
                character.getCharacterClass() != null ? character.getCharacterClass() : "Не указан",
                character.getLevel(),
                character.getDescription() != null ? character.getDescription() : "Нет",
                character.getId()
        );
        
        bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharacterDetailsKeyboard(character.getId()));
    }

    // === Создание группы ===
    
    private void handleGroupName(Long chatId, String text, CharacterGroupBot bot) {
        userStateService.setData(chatId, "group_name", text);
        userStateService.setState(chatId, State.CREATING_GROUP_DESCRIPTION);
        bot.sendMessageWithKeyboard(chatId, 
                "📝 Введите описание группы:\n\nИли отправьте '-' чтобы пропустить",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleGroupDescription(Long chatId, String text, CharacterGroupBot bot) {
        String description = text.equals("-") ? null : text;
        
        String name = userStateService.getDataAsString(chatId, "group_name");
        
        Long telegramId = chatId;
        User user = userService.getUserByTelegramId(telegramId);
        
        Group group = groupService.createGroup(user, name, description);
        
        userStateService.clearState(chatId);
        
        String response = String.format("""
                ✅ Группа создана!
                
                👥 %s
                📝 Описание: %s
                👑 Лидер: %s
                🆔 ID: %d
                
                Теперь вы можете добавить участников в группу.
                """,
                group.getName(),
                group.getDescription() != null ? group.getDescription() : "Нет",
                user.getUsername(),
                group.getId()
        );
        
        bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupDetailsKeyboard(group.getId()));
    }

    // === Добавление участника ===
    
    private void handleAddMemberCharacterId(Long chatId, String text, CharacterGroupBot bot) {
        Long groupId = userStateService.getDataAsLong(chatId, "group_id");
        
        try {
            Long characterId = Long.parseLong(text);
            
            Long telegramId = chatId;
            User user = userService.getUserByTelegramId(telegramId);
            Group group = groupService.getGroupById(groupId, user);
            Character character = characterService.getCharacterByIdAnyUser(characterId);
            
            groupService.addMemberToGroup(group, character);
            
            userStateService.clearState(chatId);
            
            String response = String.format("""
                    ✅ Участник добавлен!
                    
                    👤 Персонаж: %s
                    👥 Игрок: %s
                    📊 Уровень: %d
                    """,
                    character.getName(),
                    character.getUser().getUsername(),
                    character.getLevel()
            );
            
            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getGroupDetailsKeyboard(groupId));
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Введите числовой ID персонажа");
        }
    }
    
    // === Присоединение к группе по коду ===
    
    private void handleJoiningGroupCode(Long chatId, String code, CharacterGroupBot bot) {
        try {
            // Проверяем валидность кода
            var invitation = groupService.getValidInvitation(code);
            
            Long telegramId = chatId;
            User user = userService.getUserByTelegramId(telegramId);
            var characters = characterService.getUserCharacters(user);
            
            if (characters.isEmpty()) {
                userStateService.clearState(chatId);
                bot.sendMessageWithKeyboard(chatId, 
                        "❌ У вас нет персонажей!\n\nСначала создайте персонажа в меню Персонажи.",
                        KeyboardFactory.getMainMenuKeyboard());
                return;
            }
            
            userStateService.clearState(chatId);
            
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
                    KeyboardFactory.getJoinGroupSelectCharacterKeyboard(code.toUpperCase(), characters));
        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ " + e.getMessage());
        }
    }

    // === Редактирование персонажа ===
    
    private void handleEditCharacterName(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            userStateService.setData(chatId, "new_name", text);
        }
        userStateService.setState(chatId, State.EDITING_CHARACTER_CLASS);
        bot.sendMessageWithKeyboard(chatId, 
                "⚔️ Введите новый класс:\n\nИли отправьте '-' чтобы оставить текущий",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEditCharacterClass(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            userStateService.setData(chatId, "new_class", text);
        }
        userStateService.setState(chatId, State.EDITING_CHARACTER_LEVEL);
        bot.sendMessageWithKeyboard(chatId, 
                "📊 Введите новый уровень (число):\n\nИли отправьте '-' чтобы оставить текущий",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEditCharacterLevel(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            try {
                int level = Integer.parseInt(text);
                userStateService.setData(chatId, "new_level", level);
            } catch (NumberFormatException e) {
                bot.sendMessage(chatId, "❌ Введите число или '-'");
                return;
            }
        }
        userStateService.setState(chatId, State.EDITING_CHARACTER_DESCRIPTION);
        bot.sendMessageWithKeyboard(chatId, 
                "📝 Введите новое описание:\n\nИли отправьте '-' чтобы оставить текущее",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEditCharacterDescription(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            userStateService.setData(chatId, "new_description", text);
        }
        
        Long characterId = userStateService.getDataAsLong(chatId, "character_id");
        String newName = userStateService.getDataAsString(chatId, "new_name");
        String newClass = userStateService.getDataAsString(chatId, "new_class");
        Integer newLevel = userStateService.getDataAsInt(chatId, "new_level");
        String newDescription = userStateService.getDataAsString(chatId, "new_description");
        
        Long telegramId = chatId;
        User user = userService.getUserByTelegramId(telegramId);
        Character character = characterService.getCharacterById(characterId, user);
        
        // Используем новые значения или текущие
        String finalName = newName != null ? newName : character.getName();
        String finalClass = newClass != null ? newClass : character.getCharacterClass();
        Integer finalLevel = newLevel != null ? newLevel : character.getLevel();
        String finalDescription = newDescription != null ? newDescription : character.getDescription();
        
        character = characterService.updateCharacter(characterId, user, finalName, finalClass, finalLevel, finalDescription);
        
        userStateService.clearState(chatId);
        
        String response = String.format("""
                ✅ Персонаж обновлен!
                
                👤 %s
                ⚔️ Класс: %s
                📊 Уровень: %d
                📝 Описание: %s
                """,
                character.getName(),
                character.getCharacterClass() != null ? character.getCharacterClass() : "Не указан",
                character.getLevel(),
                character.getDescription() != null ? character.getDescription() : "Нет"
        );
        
        bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharacterDetailsKeyboard(characterId));
    }

    // === Редактирование желания ===
    
    private void handleEditWishName(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            userStateService.setData(chatId, "new_wish_name", text);
        }
        userStateService.setState(chatId, State.EDITING_WISH_TYPE);
        bot.sendMessageWithKeyboard(chatId, 
                "📦 Введите новый тип:\n\nИли отправьте '-' чтобы оставить текущий",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEditWishType(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            userStateService.setData(chatId, "new_wish_type", text);
        }
        userStateService.setState(chatId, State.EDITING_WISH_PRIORITY);
        bot.sendMessageWithKeyboard(chatId, 
                "🔥 Введите новый приоритет (1-10):\n\nИли отправьте '-' чтобы оставить текущий",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEditWishPriority(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            try {
                int priority = Integer.parseInt(text);
                if (priority < 1 || priority > 10) {
                    bot.sendMessage(chatId, "❌ Приоритет должен быть от 1 до 10");
                    return;
                }
                userStateService.setData(chatId, "new_wish_priority", priority);
            } catch (NumberFormatException e) {
                bot.sendMessage(chatId, "❌ Введите число от 1 до 10 или '-'");
                return;
            }
        }
        userStateService.setState(chatId, State.EDITING_WISH_NOTES);
        bot.sendMessageWithKeyboard(chatId, 
                "📝 Введите новые заметки:\n\nИли отправьте '-' чтобы оставить текущие",
                KeyboardFactory.getCancelKeyboard());
    }

    private void handleEditWishNotes(Long chatId, String text, CharacterGroupBot bot) {
        if (!text.equals("-")) {
            userStateService.setData(chatId, "new_wish_notes", text);
        }
        
        Long wishId = userStateService.getDataAsLong(chatId, "wish_id");
        Long characterId = userStateService.getDataAsLong(chatId, "character_id");
        String newName = userStateService.getDataAsString(chatId, "new_wish_name");
        String newType = userStateService.getDataAsString(chatId, "new_wish_type");
        Integer newPriority = userStateService.getDataAsInt(chatId, "new_wish_priority");
        String newNotes = userStateService.getDataAsString(chatId, "new_wish_notes");
        
        Wishlist wish = wishlistService.getWishById(wishId);
        
        // Используем новые значения или текущие
        String finalName = newName != null ? newName : wish.getItemName();
        String finalType = newType != null ? newType : wish.getItemType();
        Integer finalPriority = newPriority != null ? newPriority : wish.getPriority();
        String finalNotes = newNotes != null ? newNotes : wish.getNotes();
        
        wishlistService.updateWish(wishId, finalName, finalType, finalPriority, finalNotes);
        
        userStateService.clearState(chatId);
        
        String response = String.format("""
                ✅ Желание обновлено!
                
                ⭐ %s
                📦 Тип: %s
                🔥 Приоритет: %d
                📝 Заметки: %s
                """,
                finalName,
                finalType != null ? finalType : "Не указан",
                finalPriority,
                finalNotes != null ? finalNotes : "Нет"
        );
        
        bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getCharacterDetailsKeyboard(characterId));
    }

    // === Вспомогательные методы ===
    
    public void startAddingEquipment(Long chatId, Long characterId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setData(chatId, "character_id", characterId);
        userStateService.setState(chatId, State.ADDING_EQUIPMENT_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Добавление экипировки\n\n⚔️ Введите название предмета:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    public void startAddingWish(Long chatId, Long characterId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setData(chatId, "character_id", characterId);
        userStateService.setState(chatId, State.ADDING_WISH_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Добавление желания\n\n⭐ Введите название желаемого предмета:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    public void startCreatingCharacter(Long chatId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setState(chatId, State.CREATING_CHARACTER_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Создание персонажа\n\n👤 Введите имя персонажа:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    public void startCreatingGroup(Long chatId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setState(chatId, State.CREATING_GROUP_NAME);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Создание группы\n\n👥 Введите название группы:",
                KeyboardFactory.getCancelKeyboard());
    }
    
    public void startAddingMember(Long chatId, Long groupId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        userStateService.setData(chatId, "group_id", groupId);
        userStateService.setState(chatId, State.ADDING_MEMBER_CHARACTER_ID);
        bot.sendMessageWithKeyboard(chatId, 
                "➕ Добавление участника\n\n👤 Введите ID персонажа:\n\n💡 Попросите игрока узнать ID через /mycharacters",
                KeyboardFactory.getCancelKeyboard());
    }
    
    public void cancelOperation(Long chatId, CharacterGroupBot bot) {
        userStateService.clearState(chatId);
        bot.sendMessageWithKeyboard(chatId, "❌ Операция отменена", KeyboardFactory.getMainMenuKeyboard());
    }

    @Override
    public String getCommand() {
        return "interactive_input";
    }
}
