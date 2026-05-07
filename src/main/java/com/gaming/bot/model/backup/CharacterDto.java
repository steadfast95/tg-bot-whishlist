package com.gaming.bot.model.backup;

import com.gaming.bot.model.Character;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterDto {
    
    private Long id;
    private Long userId;
    private String name;
    private String characterClass;
    private Integer level;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    
    public static CharacterDto fromEntity(Character character) {
        return CharacterDto.builder()
                .id(character.getId())
                .userId(character.getUser().getId())
                .name(character.getName())
                .characterClass(character.getCharacterClass())
                .level(character.getLevel())
                .description(character.getDescription())
                .createdAt(character.getCreatedAt())
                .updatedAt(character.getUpdatedAt())
                .isActive(character.getIsActive())
                .build();
    }
}
