package com.gaming.bot.telegram.handler;

import com.gaming.bot.service.EquipmentService;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteEquipmentHandler implements CommandHandler {

    private final EquipmentService equipmentService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/deleteequipment");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID предмета: /deleteequipment 5");
            return;
        }

        try {
            Long equipmentId = Long.parseLong(parts[1].trim());

            equipmentService.removeEquipment(equipmentId);

            String response = """
                    ✅ Предмет удален!
                    
                    Предмет больше не отображается в экипировке.
                    """;

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getEquipmentMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID предмета");
        } catch (Exception e) {
            log.error("Error deleting equipment", e);
            bot.sendMessage(chatId, "❌ Ошибка при удалении предмета: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/deleteequipment";
    }
}
