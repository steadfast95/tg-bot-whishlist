package com.gaming.bot.model.backup;

import com.gaming.bot.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime registeredAt;
    private LocalDateTime lastActivity;
    
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .telegramId(user.getTelegramId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .registeredAt(user.getRegisteredAt())
                .lastActivity(user.getLastActivity())
                .build();
    }
    
    public User toEntity() {
        return User.builder()
                .id(id)
                .telegramId(telegramId)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .registeredAt(registeredAt)
                .lastActivity(lastActivity)
                .build();
    }
}
