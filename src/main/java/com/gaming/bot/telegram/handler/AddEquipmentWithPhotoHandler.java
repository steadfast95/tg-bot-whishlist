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
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddEquipmentWithPhotoHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;
    private final EquipmentService equipmentService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasPhoto() &&
               update.getMessage().getCaption() != null &&
               update.getMessage().getCaption().startsWith("/addequipment");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String caption = update.getMessage().getCaption();

        List<PhotoSize> photos = update.getMessage().getPhoto();
        String fileId = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse(null);

        if (fileId == null) {
            bot.sendMessage(chatId, "❌ Не удалось получить изображение");
            return;
        }

        String[] parts = caption.split("\\|", 6);
        
        if (parts.length < 3) {
            String usage = """
                    ⚔️ Добавление экипировки с фото
                    
                    1. Прикрепите фото предмета
                    2. В подписи к фото укажите:
                    
                    Формат: /addequipment ID_персонажа|название|тип|количество|описание
                    
                    Пример подписи:
                    /addequipment 1|Меч Андуил|Оружие|1|Легендарный меч
                    
                    Минимум: /addequipment 1|Меч Андуил
                    
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

            Equipment equipment = equipmentService.addEquipmentWithImage(
                    character, itemName, itemType, quantity, description, fileId
            );

            String response = String.format("""
                    ✅ Экипировка с изображением добавлена!
                    
                    ⚔️ Предмет: %s
                    📦 Тип: %s
                    🔢 Количество: %d
                    📝 Описание: %s
                    📸 Изображение: прикреплено
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
            log.error("Error adding equipment with photo", e);
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
        return "/addequipment_photo";
    }
}
