package com.gaming.bot.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    @ConfigurationProperties(prefix = "telegram.bot")
    public BotProperties botProperties() {
        return new BotProperties();
    }

    @Data
    public static class BotProperties {
        private String name;
        private String token;
        private String adminSecret;
        
        @PostConstruct
        public void logAdminSecret() {
            String secret = getEffectiveAdminSecret();
            log.info("===========================================");
            log.info("Admin secret for database export/import: {}", secret);
            log.info("Use: /exportdb {} or /importdb {}", secret, secret);
            log.info("===========================================");
        }
        
        /**
         * Возвращает секрет администратора.
         * Если задан в конфиге — использует его, иначе генерирует из токена бота.
         */
        public String getEffectiveAdminSecret() {
            if (adminSecret != null && !adminSecret.isBlank()) {
                return adminSecret;
            }
            // Генерируем секрет из токена бота (первые 16 символов MD5)
            return DigestUtils.md5DigestAsHex(token.getBytes()).substring(0, 16);
        }
    }
}
