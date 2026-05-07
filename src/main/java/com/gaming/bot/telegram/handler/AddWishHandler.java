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

@Slf4j
@Component
@RequiredArgsConstructor
public class AddWishHandler implements CommandHandler {

    private final UserService userService;
    private final CharacterService characterService;
    private final WishlistService wishlistService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/addwish");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long telegramId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        String[] parts = messageText.split("\\|", 6);
        
        if (parts.length < 3) {
            String usage = """
                    ⭐ Добавление желания
                    
                    Формат: /addwish ID_персонажа|название|тип|приоритет|заметки
                    
                    Пример:
                    /addwish 1|Щит Элендила|Щит|5|Легендарный щит гондорцев
                    
                    Минимум: /addwish 1|Щит Элендила
                    
                    Приоритет от 1 до 10 (10 - самый высокий)
                    Используйте /mycharacters чтобы узнать ID персонажа
                    """;
            bot.sendMessage(chatId, usage);
            return;
        }

        try {
            User user = userService.getUserByTelegramId(telegramId);
            
            Long characterId = Long.parseLong(parts[0].replace("/addwish", "").trim());
            Character character = characterService.getCharacterById(characterId, user);
            
            String itemName = parts[1].trim();
            String itemType = parts.length > 2 ? parts[2].trim() : null;
            Integer priority = parts.length > 3 ? parseInteger(parts[3].trim()) : 1;
            String notes = parts.length > 4 ? parts[4].trim() : null;

            Wishlist wish = wishlistService.addWish(
                    character, itemName, itemType, priority, notes
            );

            String response = String.format("""
                    ✅ Желание добавлено!
                    
                    ⭐ Предмет: %s
                    📦 Тип: %s
                    🔥 Приоритет: %d
                    📝 Заметки: %s
                    👤 Персонаж: %s
                    """,
                    wish.getItemName(),
                    wish.getItemType() != null ? wish.getItemType() : "Не указан",
                    wish.getPriority(),
                    wish.getNotes() != null ? wish.getNotes() : "Нет заметок",
                    character.getName()
            );

            bot.sendMessageWithKeyboard(chatId, response, KeyboardFactory.getWishesMenuKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Неверный формат ID персонажа");
        } catch (Exception e) {
            log.error("Error adding wish", e);
            bot.sendMessage(chatId, "❌ Ошибка при добавлении желания: " + e.getMessage());
        }
    }

    private Integer parseInteger(String value) {
        try {
            int val = Integer.parseInt(value);
            return Math.min(Math.max(val, 1), 10);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public String getCommand() {
        return "/addwish";
    }
}
