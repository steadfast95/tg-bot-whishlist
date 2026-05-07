package com.gaming.bot.service;

import com.gaming.bot.model.User;
import com.gaming.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(Long telegramId, String username, String firstName, String lastName) {
        return userRepository.findByTelegramId(telegramId)
                .map(user -> {
                    user.setLastActivity(LocalDateTime.now());
                    user.setUsername(username);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .telegramId(telegramId)
                            .username(username)
                            .firstName(firstName)
                            .lastName(lastName)
                            .registeredAt(LocalDateTime.now())
                            .lastActivity(LocalDateTime.now())
                            .build();
                    log.info("Creating new user: telegramId={}, username={}", telegramId, username);
                    return userRepository.save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public User getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found: " + telegramId));
    }
}
