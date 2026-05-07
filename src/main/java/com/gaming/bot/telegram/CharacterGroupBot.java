package com.gaming.bot.telegram;

import com.gaming.bot.config.BotConfig;
import com.gaming.bot.telegram.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CharacterGroupBot extends TelegramLongPollingBot implements InitializingBean, DisposableBean {

    private final String botUsername;
    private final TelegramBotsApi telegramBotsApi;
    private final List<CommandHandler> handlers;
    private BotSession botSession;

    public CharacterGroupBot(
            TelegramBotsApi telegramBotsApi,
            BotConfig.BotProperties botProperties,
            List<CommandHandler> handlers
    ) {
        super(botProperties.getToken());
        this.botUsername = botProperties.getName();
        this.telegramBotsApi = telegramBotsApi;
        this.handlers = new ArrayList<>(handlers);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            log.info("Registering bot: {}", botUsername);
            botSession = telegramBotsApi.registerBot(this);
            setupCommands();
            log.info("Bot registered successfully: {}", botUsername);
        } catch (TelegramApiException e) {
            log.error("Failed to register bot", e);
            throw new RuntimeException("Bot registration failed", e);
        }
    }

    private void setupCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("/start", "Начать работу с ботом"),
                new BotCommand("/help", "Помощь"),
                new BotCommand("/createcharacter", "Создать персонажа"),
                new BotCommand("/mycharacters", "Мои персонажи"),
                new BotCommand("/addequipment", "Добавить экипировку"),
                new BotCommand("/myequipment", "Моя экипировка"),
                new BotCommand("/addwish", "Добавить желание"),
                new BotCommand("/mywishes", "Мои желания"),
                new BotCommand("/creategroup", "Создать группу"),
                new BotCommand("/mygroups", "Мои группы"),
                new BotCommand("/groupinfo", "Информация о группе"),
                new BotCommand("/join", "Присоединиться к группе по коду")
        );

        try {
            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
            log.info("Bot commands set successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to set bot commands", e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            handlers.stream()
                    .filter(handler -> handler.canHandle(update))
                    .findFirst()
                    .ifPresentOrElse(
                            handler -> handler.handle(update, this),
                            () -> {
                                if (update.hasMessage() && update.getMessage().hasText()) {
                                    sendMessage(update.getMessage().getChatId(), 
                                            "Неизвестная команда. Используйте /help для списка команд.");
                                }
                            }
                    );
        } catch (Exception e) {
            log.error("Error processing update", e);
            if (update.hasMessage()) {
                sendMessage(update.getMessage().getChatId(), 
                        "Произошла ошибка при обработке команды. Попробуйте еще раз.");
            }
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat: {}", chatId, e);
        }
    }

    public void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(keyboard)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message with keyboard to chat: {}", chatId, e);
        }
    }

    public void answerCallbackQuery(String callbackQueryId) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query", e);
        }
    }

    public void sendPhoto(Long chatId, String fileId, String caption) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId.toString())
                .photo(new InputFile(fileId))
                .caption(caption)
                .build();
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Failed to send photo to chat: {}", chatId, e);
        }
    }

    public void executeMessage(EditMessageText editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message", e);
        }
    }

    public void sendDocument(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Failed to send document", e);
        }
    }

    public org.telegram.telegrambots.meta.api.objects.File getFile(String fileId) throws TelegramApiException {
        return execute(new GetFile(fileId));
    }

    public String getBotToken() {
        return super.getBotToken();
    }

    @Override
    public void destroy() throws Exception {
        if (botSession != null && botSession.isRunning()) {
            botSession.stop();
            log.info("Bot session stopped");
        }
    }
}
