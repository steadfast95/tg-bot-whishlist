package com.gaming.bot.telegram.handler;

import com.gaming.bot.telegram.CharacterGroupBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class HelpCommandHandler implements CommandHandler {

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/help");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        Long chatId = update.getMessage().getChatId();

        String helpMessage = """
                📖 Справка по командам:
                
                👤 Персонажи:
                /createcharacter - Создать персонажа
                /mycharacters - Список персонажей
                
                ⚔️ Экипировка:
                /addequipment - Добавить предмет (с фото или без)
                /myequipment - Посмотреть экипировку
                
                ⭐ Желания:
                /addwish - Добавить желание
                /mywishes - Список желаний
                
                👥 Группы:
                /creategroup - Создать группу
                /mygroups - Мои группы
                /groupinfo - Информация о группе
                /join КОД - Присоединиться к группе по коду
                
                🔧 Администрирование:
                /exportdb КЛЮЧ - Экспорт базы данных
                /importdb КЛЮЧ - Импорт базы данных
                
                💡 Как пригласить игрока в группу:
                1. Лидер группы нажимает "Пригласить (код)" - получает код
                2. Лидер отправляет код игроку
                3. Игрок вводит /join КОД или использует меню
                4. Игрок выбирает персонажа для вступления
                
                ⏱️ Код приглашения действует 15 минут.
                """;

        bot.sendMessage(chatId, helpMessage);
    }

    @Override
    public String getCommand() {
        return "/help";
    }
}
