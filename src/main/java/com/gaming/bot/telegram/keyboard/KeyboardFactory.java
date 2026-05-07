package com.gaming.bot.telegram.keyboard;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.GroupMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public static InlineKeyboardMarkup getMainMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("👤 Персонажи", "menu_characters"),
                createButton("👥 Группы", "menu_groups")
        ));
        keyboard.add(List.of(
                createButton("ℹ️ Помощь", "menu_help")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getCharactersMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("➕ Создать персонажа", "create_character")
        ));
        keyboard.add(List.of(
                createButton("📋 Мои персонажи", "my_characters")
        ));
        keyboard.add(List.of(
                createButton("🔙 Главное меню", "main_menu")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getEquipmentMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("➕ Добавить предмет", "add_equipment_prompt")
        ));
        keyboard.add(List.of(
                createButton("📋 Посмотреть экипировку", "view_equipment_prompt")
        ));
        keyboard.add(List.of(
                createButton("🗑️ Удалить предмет", "delete_equipment_prompt")
        ));
        keyboard.add(List.of(
                createButton("🔙 Главное меню", "main_menu")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getWishesMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("➕ Добавить желание", "add_wish_prompt")
        ));
        keyboard.add(List.of(
                createButton("📋 Посмотреть желания", "view_wishes_prompt")
        ));
        keyboard.add(List.of(
                createButton("✅ Отметить выполненным", "fulfill_wish_prompt"),
                createButton("🗑️ Удалить желание", "delete_wish_prompt")
        ));
        keyboard.add(List.of(
                createButton("🔙 Главное меню", "main_menu")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getGroupsMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("➕ Создать группу", "create_group_prompt")
        ));
        keyboard.add(List.of(
                createButton("🔗 Присоединиться по коду", "join_group_prompt")
        ));
        keyboard.add(List.of(
                createButton("📋 Мои группы", "my_groups")
        ));
        keyboard.add(List.of(
                createButton("🔙 Главное меню", "main_menu")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getBackToMainMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("🔙 Главное меню", "main_menu")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getCharactersListKeyboard(java.util.List<com.gaming.bot.model.Character> characters) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (com.gaming.bot.model.Character character : characters) {
            String buttonText = String.format("%s | ID: %d", character.getName(), character.getId());
            keyboard.add(List.of(
                    createButton(buttonText, "view_char_" + character.getId())
            ));
        }

        keyboard.add(List.of(
                createButton("🔙 Меню персонажей", "menu_characters")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getCharacterDetailsKeyboard(Long characterId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("⚔️ Экипировка", "view_equipment_for_" + characterId),
                createButton("⭐ Желания", "view_wishes_for_" + characterId)
        ));
        keyboard.add(List.of(
                createButton("➕ Добавить экипировку", "add_equipment_for_" + characterId),
                createButton("➕ Добавить желание", "add_wish_for_" + characterId)
        ));
        keyboard.add(List.of(
                createButton("✏️ Редактировать", "edit_character_for_" + characterId),
                createButton("🗑️ Удалить", "delete_character_" + characterId)
        ));
        keyboard.add(List.of(
                createButton("🔙 К списку персонажей", "my_characters")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getGroupsListKeyboard(java.util.List<com.gaming.bot.model.Group> groups) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (com.gaming.bot.model.Group group : groups) {
            String buttonText = String.format("%s | ID: %d", group.getName(), group.getId());
            keyboard.add(List.of(
                    createButton(buttonText, "view_group_" + group.getId())
            ));
        }

        keyboard.add(List.of(
                createButton("🔙 Меню групп", "menu_groups")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getGroupDetailsKeyboard(Long groupId) {
        return getGroupDetailsKeyboard(groupId, true);
    }
    
    public static InlineKeyboardMarkup getGroupDetailsKeyboard(Long groupId, boolean isLeader) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("👥 Участники и экипировка", "group_members_" + groupId + "_0")
        ));
        
        if (isLeader) {
            keyboard.add(List.of(
                    createButton("🔗 Пригласить (код)", "add_member_for_" + groupId)
            ));
            keyboard.add(List.of(
                    createButton("👑 Передать лидерство", "transfer_leadership_for_" + groupId),
                    createButton("🗑️ Удалить группу", "delete_group_" + groupId)
            ));
        } else {
            keyboard.add(List.of(
                    createButton("🚪 Выйти из группы", "leave_group_" + groupId)
            ));
        }
        
        keyboard.add(List.of(
                createButton("🔙 К списку групп", "my_groups")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getGroupMemberNavigationKeyboard(Long groupId, int currentIndex, int totalMembers, Long memberId) {
        return getGroupMemberNavigationKeyboard(groupId, currentIndex, totalMembers, memberId, true);
    }
    
    public static InlineKeyboardMarkup getGroupMemberNavigationKeyboard(Long groupId, int currentIndex, int totalMembers, Long memberId, boolean isLeader) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> navButtons = new ArrayList<>();
        if (currentIndex > 0) {
            navButtons.add(createButton("⬅️ Пред.", "group_members_" + groupId + "_" + (currentIndex - 1)));
        }
        navButtons.add(createButton(String.format("%d/%d", currentIndex + 1, totalMembers), "noop"));
        if (currentIndex < totalMembers - 1) {
            navButtons.add(createButton("След. ➡️", "group_members_" + groupId + "_" + (currentIndex + 1)));
        }
        keyboard.add(navButtons);

        if (isLeader) {
            keyboard.add(List.of(
                    createButton("🗑️ Удалить из группы", "remove_member_" + groupId + "_" + memberId)
            ));
        }
        keyboard.add(List.of(
                createButton("📋 Показать всех", "group_all_members_" + groupId)
        ));
        keyboard.add(List.of(
                createButton("🔙 К группе", "view_group_" + groupId)
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getGroupAllMembersKeyboard(Long groupId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("👤 По одному", "group_members_" + groupId + "_0")
        ));
        keyboard.add(List.of(
                createButton("🔙 К группе", "view_group_" + groupId)
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getDeleteGroupConfirmKeyboard(Long groupId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("✅ Да, удалить", "confirm_delete_group_" + groupId),
                createButton("❌ Отмена", "view_group_" + groupId)
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getLeaveGroupConfirmKeyboard(Long groupId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("✅ Да, выйти", "confirm_leave_group_" + groupId),
                createButton("❌ Отмена", "view_group_" + groupId)
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getDeleteCharacterConfirmKeyboard(Long characterId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("✅ Да, удалить", "confirm_delete_character_" + characterId),
                createButton("❌ Отмена", "view_char_" + characterId)
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getWishNavigationKeyboard(Long characterId, int currentIndex, int totalWishes, Long wishId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Навигация
        List<InlineKeyboardButton> navButtons = new ArrayList<>();
        if (currentIndex > 0) {
            navButtons.add(createButton("⬅️ Пред.", "wish_nav_" + characterId + "_" + (currentIndex - 1)));
        }
        navButtons.add(createButton(String.format("%d/%d", currentIndex + 1, totalWishes), "noop"));
        if (currentIndex < totalWishes - 1) {
            navButtons.add(createButton("След. ➡️", "wish_nav_" + characterId + "_" + (currentIndex + 1)));
        }
        keyboard.add(navButtons);

        // Действия с желанием
        keyboard.add(List.of(
                createButton("✅ Выполнено", "fulfill_wish_" + wishId + "_" + characterId),
                createButton("✏️ Изменить", "edit_wish_" + wishId + "_" + characterId)
        ));
        keyboard.add(List.of(
                createButton("🗑️ Удалить", "delete_wish_" + wishId + "_" + characterId)
        ));
        keyboard.add(List.of(
                createButton("🔙 К персонажу", "view_char_" + characterId)
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getDeleteWishConfirmKeyboard(Long wishId, Long characterId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("✅ Да, удалить", "confirm_delete_wish_" + wishId + "_" + characterId),
                createButton("❌ Отмена", "view_wishes_for_" + characterId)
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getCancelKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(
                createButton("❌ Отмена", "cancel_operation")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }
    
    public static InlineKeyboardMarkup getTransferLeadershipKeyboard(Long groupId, List<GroupMember> members) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        for (GroupMember member : members) {
            String buttonText = String.format("👤 %s (%s)", 
                    member.getCharacter().getName(),
                    member.getCharacter().getUser().getUsername());
            keyboard.add(List.of(
                    createButton(buttonText, "confirm_transfer_" + groupId + "_" + member.getId())
            ));
        }
        
        keyboard.add(List.of(
                createButton("🔙 Отмена", "view_group_" + groupId)
        ));
        
        markup.setKeyboard(keyboard);
        return markup;
    }
    
    public static InlineKeyboardMarkup getJoinGroupSelectCharacterKeyboard(String invitationCode, List<Character> characters) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        for (Character character : characters) {
            String buttonText = String.format("👤 %s (Ур. %d)", 
                    character.getName(),
                    character.getLevel());
            keyboard.add(List.of(
                    createButton(buttonText, "join_with_character_" + invitationCode + "_" + character.getId())
            ));
        }
        
        keyboard.add(List.of(
                createButton("🔙 Отмена", "menu_groups")
        ));
        
        markup.setKeyboard(keyboard);
        return markup;
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
