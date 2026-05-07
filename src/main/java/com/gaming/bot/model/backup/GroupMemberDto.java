package com.gaming.bot.model.backup;

import com.gaming.bot.model.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDto {
    
    private Long id;
    private Long groupId;
    private Long characterId;
    private LocalDateTime joinedAt;
    private Boolean isActive;
    
    public static GroupMemberDto fromEntity(GroupMember member) {
        return GroupMemberDto.builder()
                .id(member.getId())
                .groupId(member.getGroup().getId())
                .characterId(member.getCharacter().getId())
                .joinedAt(member.getJoinedAt())
                .isActive(member.getIsActive())
                .build();
    }
}
