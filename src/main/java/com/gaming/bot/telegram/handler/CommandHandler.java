package com.gaming.bot.telegram.handler;

import com.gaming.bot.telegram.CharacterGroupBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {
    
    boolean canHandle(Update update);
    
    void handle(Update update, CharacterGroupBot bot);
    
    String getCommand();
}
