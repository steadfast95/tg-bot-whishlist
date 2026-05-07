package com.gaming.bot.model.backup;

import com.gaming.bot.model.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    
    private Long id;
    private Long leaderId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    
    public static GroupDto fromEntity(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .leaderId(group.getLeader().getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .isActive(group.getIsActive())
                .build();
    }
}
