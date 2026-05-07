package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Equipment;
import com.gaming.bot.model.User;
import com.gaming.bot.service.CharacterService;
import com.gaming.bot.service.EquipmentService;
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
public class AddEquipmentHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;
    private final EquipmentService equipmentService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/addequipment");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split("\\|", 6);
        
        if (parts.length < 3) {
            String usage = """
                    ⚔️ Добавление экипировки
                    
                    📝 Текстовый вариант:
                    Формат: /addequipment ID_персонажа|название|тип|количество|описание
                    
                    Пример:
                    /addequipment 1|Меч Андуил|Оружие|1|Легендарный меч
                    
                    Минимум: /addequipment 1|Меч Андуил
                    
                    📸 С изображением:
                    1. Прикрепите фото предмета
                    2. В подписи укажите команду в том же формате
                    
                    Используйте /mycharacters чтобы узнать ID персонажа
                    """;
            bot.sendMessage(chatId, usage);
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            
            Long characterId = Long.parseLong(parts[0].replace("/addequipment", "").trim());
            Character character = characterService.getCharacterById(characterId, user);
            
            String itemName = parts[1].trim();
            String itemType = parts.length > 2 ? parts[2].trim() : null;
            Integer quantity = parts.length > 3 ? parseInteger(parts[3].trim()) : 1;
            String description = parts.length > 4 ? parts[4].trim() : null;

            Equipment equipment = equipmentService.addEquipment(
                    character, itemName, itemType, quantity, description
            );

            String response = String.format("""
                    ✅ Экипировка добавлена!
                    
                    ⚔️ Предмет: %s
                    📦 Тип: %s
                    🔢 Количество: %d
                    📝 Описание: %s
                    👤 Персонаж: %s
                    """,
                    equipment.getItemName(),
                    equipment.getItemType() != null ? equipment.getItemType() : "Не указан",
                    equipment.getQuantity(),
                    equipment.getDescription() != null ? equipment.getDescription() : "Нет описания",
                    character.getName()
            );

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getEquipmentMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID персонажа");
        } catch (Exception e) {
            log.error("Error adding equipment", e);
            bot.sendMessage(chatId, "❌ Ошибка при добавлении экипировки: " + e.getMessage());
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public String getCommand() {
        return "/addequipment";
    }
}
