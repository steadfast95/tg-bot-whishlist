package com.gaming.bot.telegram.handler;

import com.gaming.bot.service.WishlistService;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillWishHandler implements CommandHandler {

    private final WishlistService wishlistService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/fulfillwish");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID желания: /fulfillwish 3");
            return;
        }

        try {
            Long wishId = Long.parseLong(parts[1].trim());

            wishlistService.fulfillWish(wishId);

            String response = """
                    ✅ Желание отмечено как выполненное!
                    
                    Поздравляем с получением желаемого предмета! 🎉
                    """;

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getWishesMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID желания");
        } catch (Exception e) {
            log.error("Error fulfilling wish", e);
            bot.sendMessage(chatId, "❌ Ошибка при отметке желания: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/fulfillwish";
    }
}
