package com.gaming.bot.telegram.handler;

import com.gaming.bot.service.UserService;
import com.gaming.bot.telegram.CharacterGroupBot;
import com.gaming.bot.telegram.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private final UserService userService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() && 
               update.getMessage().getText().startsWith("/start");
    }

    @Override
    public void handle(Update update, CharacterGroupBot bot) {
        User telegramUser = update.getMessage().getFrom();
        Long chatId = update.getMessage().getChatId();

        userService.getOrCreateUser(
                telegramUser.getId(),
                telegramUser.getUserName(),
                telegramUser.getFirstName(),
                telegramUser.getLastName()
        );

        String welcomeMessage = """
                Добро пожаловать в Character Group Bot! 🎮
                
                Этот бот поможет вам управлять персонажами и группами.
                
                🎯 Основные возможности:
                • Создание и управление персонажами
                • Добавление экипировки (с фото!)
                • Список желаемых предметов
                • Групповые рейды с обзором снаряжения
                
                💡 Используйте кнопки ниже для навигации!
                Текстовые команды тоже доступны - /help для списка.
                """;

        bot.sendMessageWithKeyboard(chatId, welcomeMessage, KeyboardFactory.getMainMenuKeyboard());
    }

    @Override
    public String getCommand() {
        return "/start";
    }
}
