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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyEquipmentHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;
    private final EquipmentService equipmentService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/myequipment");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID персонажа: /myequipment 1\nИспользуйте /mycharacters для получения списка");
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            Long characterId = Long.parseLong(parts[1].trim());
            Character character = characterService.getCharacterById(characterId, user);
            
            List<Equipment> equipmentList = equipmentService.getCharacterEquipment(character);

            if (equipmentList.isEmpty()) {
                bot.sendMessageWithKeyboard(chatId, 
                        String.format("У персонажа %s пока нет экипировки", character.getName()),
                        KeyboardFactory.getEquipmentMenuKeyboard());
                return;
            }

            bot.sendMessage(chatId, String.format("⚔️ Экипировка персонажа %s:\n", character.getName()));
            
            for (int i = 0; i < equipmentList.size(); i++) {
                Equipment equipment = equipmentList.get(i);
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
                        bot.sendMessageWithKeyboard(chatId, "⬆️ Это вся экипировка", 
                                KeyboardFactory.getEquipmentMenuKeyboard());
                    }
                } else {
                    if (isLast) {
                        bot.sendMessageWithKeyboard(chatId, itemInfo, KeyboardFactory.getEquipmentMenuKeyboard());
                    } else {
                        bot.sendMessage(chatId, itemInfo);
                    }
                }
            }
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID персонажа");
        } catch (Exception e) {
            log.error("Error getting equipment", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении экипировки: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/myequipment";
    }
}
