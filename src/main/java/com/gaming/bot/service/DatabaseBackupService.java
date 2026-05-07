package com.gaming.bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gaming.bot.model.*;
import com.gaming.bot.model.Character;
import com.gaming.bot.model.backup.*;
import com.gaming.bot.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupService {
    
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final EquipmentRepository equipmentRepository;
    private final WishlistRepository wishlistRepository;
    
    private ObjectMapper objectMapper;
    
    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Экспортирует все данные из БД в JSON строку
     */
    public String exportToJson() throws JsonProcessingException {
        log.info("Starting database export...");
        
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        
        List<CharacterDto> characters = characterRepository.findAll().stream()
                .map(CharacterDto::fromEntity)
                .collect(Collectors.toList());
        
        List<GroupDto> groups = groupRepository.findAll().stream()
                .map(GroupDto::fromEntity)
                .collect(Collectors.toList());
        
        List<GroupMemberDto> groupMembers = groupMemberRepository.findAll().stream()
                .map(GroupMemberDto::fromEntity)
                .collect(Collectors.toList());
        
        List<GroupInvitationDto> groupInvitations = groupInvitationRepository.findAll().stream()
                .map(GroupInvitationDto::fromEntity)
                .collect(Collectors.toList());
        
        List<EquipmentDto> equipment = equipmentRepository.findAll().stream()
                .map(EquipmentDto::fromEntity)
                .collect(Collectors.toList());
        
        List<WishlistDto> wishlists = wishlistRepository.findAll().stream()
                .map(WishlistDto::fromEntity)
                .collect(Collectors.toList());
        
        BackupData backup = BackupData.builder()
                .version(BackupData.CURRENT_VERSION)
                .exportedAt(LocalDateTime.now())
                .users(users)
                .characters(characters)
                .groups(groups)
                .groupMembers(groupMembers)
                .groupInvitations(groupInvitations)
                .equipment(equipment)
                .wishlists(wishlists)
                .build();
        
        log.info("Export completed: {} users, {} characters, {} groups, {} equipment, {} wishlists",
                users.size(), characters.size(), groups.size(), equipment.size(), wishlists.size());
        
        return objectMapper.writeValueAsString(backup);
    }
    
    /**
     * Импортирует данные из JSON строки в БД
     * ВНИМАНИЕ: Полностью заменяет существующие данные!
     */
    @Transactional
    public ImportResult importFromJson(String json) throws JsonProcessingException {
        log.info("Starting database import...");
        
        BackupData backup = objectMapper.readValue(json, BackupData.class);
        
        // Проверка версии
        if (backup.getVersion() == null || !backup.getVersion().equals(BackupData.CURRENT_VERSION)) {
            log.warn("Backup version mismatch. Expected: {}, Got: {}", 
                    BackupData.CURRENT_VERSION, backup.getVersion());
        }
        
        // Удаляем данные в правильном порядке (из-за FK)
        log.info("Clearing existing data...");
        wishlistRepository.deleteAll();
        equipmentRepository.deleteAll();
        groupInvitationRepository.deleteAll();
        groupMemberRepository.deleteAll();
        characterRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
        
        // Импортируем в правильном порядке
        log.info("Importing users...");
        Map<Long, User> userIdMap = new HashMap<>();
        for (UserDto dto : backup.getUsers()) {
            User user = dto.toEntity();
            user.setId(null); // Сбрасываем ID для автогенерации
            User saved = userRepository.save(user);
            userIdMap.put(dto.getId(), saved);
        }
        
        log.info("Importing characters...");
        Map<Long, Character> characterIdMap = new HashMap<>();
        for (CharacterDto dto : backup.getCharacters()) {
            User user = userIdMap.get(dto.getUserId());
            if (user == null) {
                log.warn("Skipping character {} - user {} not found", dto.getId(), dto.getUserId());
                continue;
            }
            Character character = Character.builder()
                    .user(user)
                    .name(dto.getName())
                    .characterClass(dto.getCharacterClass())
                    .level(dto.getLevel())
                    .description(dto.getDescription())
                    .createdAt(dto.getCreatedAt())
                    .updatedAt(dto.getUpdatedAt())
                    .isActive(dto.getIsActive())
                    .build();
            Character saved = characterRepository.save(character);
            characterIdMap.put(dto.getId(), saved);
        }
        
        log.info("Importing groups...");
        Map<Long, Group> groupIdMap = new HashMap<>();
        for (GroupDto dto : backup.getGroups()) {
            User leader = userIdMap.get(dto.getLeaderId());
            if (leader == null) {
                log.warn("Skipping group {} - leader {} not found", dto.getId(), dto.getLeaderId());
                continue;
            }
            Group group = Group.builder()
                    .leader(leader)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .createdAt(dto.getCreatedAt())
                    .updatedAt(dto.getUpdatedAt())
                    .isActive(dto.getIsActive())
                    .build();
            Group saved = groupRepository.save(group);
            groupIdMap.put(dto.getId(), saved);
        }
        
        log.info("Importing group members...");
        int groupMembersCount = 0;
        for (GroupMemberDto dto : backup.getGroupMembers()) {
            Group group = groupIdMap.get(dto.getGroupId());
            Character character = characterIdMap.get(dto.getCharacterId());
            if (group == null || character == null) {
                log.warn("Skipping group member - group or character not found");
                continue;
            }
            GroupMember member = GroupMember.builder()
                    .group(group)
                    .character(character)
                    .joinedAt(dto.getJoinedAt())
                    .isActive(dto.getIsActive())
                    .build();
            groupMemberRepository.save(member);
            groupMembersCount++;
        }
        
        log.info("Importing group invitations...");
        int invitationsCount = 0;
        for (GroupInvitationDto dto : backup.getGroupInvitations()) {
            Group group = groupIdMap.get(dto.getGroupId());
            if (group == null) {
                log.warn("Skipping invitation - group not found");
                continue;
            }
            Character usedBy = dto.getUsedByCharacterId() != null 
                    ? characterIdMap.get(dto.getUsedByCharacterId()) : null;
            GroupInvitation invitation = GroupInvitation.builder()
                    .group(group)
                    .code(dto.getCode())
                    .createdAt(dto.getCreatedAt())
                    .expiresAt(dto.getExpiresAt())
                    .isUsed(dto.getIsUsed())
                    .usedByCharacter(usedBy)
                    .usedAt(dto.getUsedAt())
                    .build();
            groupInvitationRepository.save(invitation);
            invitationsCount++;
        }
        
        log.info("Importing equipment...");
        int equipmentCount = 0;
        for (EquipmentDto dto : backup.getEquipment()) {
            Character character = characterIdMap.get(dto.getCharacterId());
            if (character == null) {
                log.warn("Skipping equipment {} - character not found", dto.getId());
                continue;
            }
            Equipment equipment = Equipment.builder()
                    .character(character)
                    .itemName(dto.getItemName())
                    .itemType(dto.getItemType())
                    .quantity(dto.getQuantity())
                    .description(dto.getDescription())
                    .imageFileId(dto.getImageFileId())
                    .addedAt(dto.getAddedAt())
                    .build();
            equipmentRepository.save(equipment);
            equipmentCount++;
        }
        
        log.info("Importing wishlists...");
        int wishlistsCount = 0;
        for (WishlistDto dto : backup.getWishlists()) {
            Character character = characterIdMap.get(dto.getCharacterId());
            if (character == null) {
                log.warn("Skipping wishlist {} - character not found", dto.getId());
                continue;
            }
            Wishlist wishlist = Wishlist.builder()
                    .character(character)
                    .itemName(dto.getItemName())
                    .itemType(dto.getItemType())
                    .priority(dto.getPriority())
                    .notes(dto.getNotes())
                    .addedAt(dto.getAddedAt())
                    .isFulfilled(dto.getIsFulfilled())
                    .build();
            wishlistRepository.save(wishlist);
            wishlistsCount++;
        }
        
        log.info("Import completed: {} users, {} characters, {} groups, {} equipment, {} wishlists",
                userIdMap.size(), characterIdMap.size(), groupIdMap.size(), equipmentCount, wishlistsCount);
        
        return ImportResult.builder()
                .usersCount(userIdMap.size())
                .charactersCount(characterIdMap.size())
                .groupsCount(groupIdMap.size())
                .groupMembersCount(groupMembersCount)
                .equipmentCount(equipmentCount)
                .wishlistsCount(wishlistsCount)
                .exportedAt(backup.getExportedAt())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ImportResult {
        private int usersCount;
        private int charactersCount;
        private int groupsCount;
        private int groupMembersCount;
        private int equipmentCount;
        private int wishlistsCount;
        private LocalDateTime exportedAt;
    }
}
