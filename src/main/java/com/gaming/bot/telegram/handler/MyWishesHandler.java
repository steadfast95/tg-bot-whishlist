package com.gaming.bot.telegram.handler;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.User;
import com.gaming.bot.model.Wishlist;
import com.gaming.bot.service.CharacterService;
import com.gaming.bot.service.UserService;
import com.gaming.bot.service.WishlistService;
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
public class MyWishesHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;
    private final WishlistService wishlistService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/mywishes");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split(" ", 2);
        
        if (parts.length < 2) {
            bot.sendMessage(chatId, "Укажите ID персонажа: /mywishes 1\nИспользуйте /mycharacters для получения списка");
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            Long characterId = Long.parseLong(parts[1].trim());
            Character character = characterService.getCharacterById(characterId, user);
            
            List<Wishlist> wishes = wishlistService.getCharacterWishes(character);

            if (wishes.isEmpty()) {
                bot.sendMessage(chatId, 
                        String.format("У персонажа %s пока нет желаний", character.getName()));
                return;
            }

            StringBuilder response = new StringBuilder(
                    String.format("⭐ Желания персонажа %s:\n\n", character.getName())
            );
            
            for (Wishlist wish : wishes) {
                response.append(String.format("""
                        🆔 ID: %d
                        ⭐ Предмет: %s
                        📦 Тип: %s
                        🔥 Приоритет: %d
                        📝 Заметки: %s
                        %s
                        
                        """,
                        wish.getId(),
                        wish.getItemName(),
                        wish.getItemType() != null ? wish.getItemType() : "Не указан",
                        wish.getPriority(),
                        wish.getNotes() != null ? wish.getNotes() : "Нет заметок",
                        wish.getIsFulfilled() ? "✅ Получено" : "⏳ В ожидании"
                ));
            }

            bot.sendMessageWithKeyboard(chatId, response.toString(), KeyboardFactory.getWishesMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID персонажа");
        } catch (Exception e) {
            log.error("Error getting wishes", e);
            bot.sendMessage(chatId, "❌ Ошибка при получении желаний: " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/mywishes";
    }
}
