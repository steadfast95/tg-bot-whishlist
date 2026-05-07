package com.gaming.bot.model.backup;

import com.gaming.bot.model.GroupInvitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInvitationDto {
    
    private Long id;
    private Long groupId;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isUsed;
    private Long usedByCharacterId;
    private LocalDateTime usedAt;
    
    public static GroupInvitationDto fromEntity(GroupInvitation invitation) {
        return GroupInvitationDto.builder()
                .id(invitation.getId())
                .groupId(invitation.getGroup().getId())
                .code(invitation.getCode())
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .isUsed(invitation.getIsUsed())
                .usedByCharacterId(invitation.getUsedByCharacter() != null 
                        ? invitation.getUsedByCharacter().getId() : null)
                .usedAt(invitation.getUsedAt())
                .build();
    }
}
