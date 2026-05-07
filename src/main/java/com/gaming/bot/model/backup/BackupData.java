package com.gaming.bot.model.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupData {
    
    private String version;
    private LocalDateTime exportedAt;
    private List<UserDto> users;
    private List<CharacterDto> characters;
    private List<GroupDto> groups;
    private List<GroupMemberDto> groupMembers;
    private List<GroupInvitationDto> groupInvitations;
    private List<EquipmentDto> equipment;
    private List<WishlistDto> wishlists;
    
    public static final String CURRENT_VERSION = "1.0";
}
