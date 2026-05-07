package com.gaming.bot.service;

import com.gaming.bot.model.*;
import com.gaming.bot.model.Character;
import com.gaming.bot.repository.GroupInvitationRepository;
import com.gaming.bot.repository.GroupMemberRepository;
import com.gaming.bot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private static final String INVITATION_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITATION_CODE_LENGTH = 6;
    private static final int INVITATION_EXPIRY_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;

    @Transactional
    public Group createGroup(User leader, String name, String description) {
        Group group = Group.builder()
                .leader(leader)
                .name(name)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        
        log.info("Creating group: name={}, leader={}", name, leader.getUsername());
        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<Group> getUserGroups(User user) {
        // Группы где пользователь лидер
        List<Group> leaderGroups = groupRepository.findByLeaderAndIsActiveTrue(user);
        
        // Группы где персонажи пользователя являются участниками
        List<Group> memberGroups = groupMemberRepository.findGroupsByUserCharacters(user);
        
        // Объединяем без дубликатов
        java.util.Set<Group> allGroups = new java.util.LinkedHashSet<>(leaderGroups);
        allGroups.addAll(memberGroups);
        
        return new java.util.ArrayList<>(allGroups);
    }

    @Transactional(readOnly = true)
    public Group getGroupById(Long groupId, User user) {
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        
        // Проверяем что пользователь либо лидер, либо его персонаж участник
        if (group.getLeader().getId().equals(user.getId())) {
            return group;
        }
        
        boolean isMember = groupMemberRepository.existsByGroupAndCharacterUser(group, user);
        if (!isMember) {
            throw new RuntimeException("You are not a member of this group");
        }
        
        return group;
    }
    
    @Transactional(readOnly = true)
    public Group getGroupByIdForLeader(Long groupId, User leader) {
        return groupRepository.findByIdAndLeader(groupId, leader)
                .orElseThrow(() -> new RuntimeException("Group not found or you are not the leader: " + groupId));
    }
    
    @Transactional(readOnly = true)
    public boolean isUserLeader(Group group, User user) {
        return group.getLeader().getId().equals(user.getId());
    }

    @Transactional
    public GroupMember addMemberToGroup(Group group, Character character) {
        groupMemberRepository.findByGroupAndCharacter(group, character)
                .ifPresent(member -> {
                    throw new RuntimeException("Character already in group");
                });
        
        GroupMember member = GroupMember.builder()
                .group(group)
                .character(character)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        
        log.info("Adding member to group: character={}, group={}", character.getName(), group.getName());
        return groupMemberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<GroupMember> getGroupMembers(Group group) {
        return groupMemberRepository.findActiveMembers(group);
    }

    @Transactional
    public void removeMemberFromGroup(Long memberId) {
        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));
        member.setIsActive(false);
        groupMemberRepository.save(member);
        log.info("Removed member from group: memberId={}", memberId);
    }

    @Transactional
    public void leaveGroup(Long groupId, User user) {
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));
        
        if (group.getLeader().getId().equals(user.getId())) {
            throw new RuntimeException("Лидер не может выйти из группы. Сначала передайте лидерство или удалите группу.");
        }
        
        // Находим все активные членства персонажей этого пользователя в группе
        List<GroupMember> memberships = groupMemberRepository.findByGroupAndCharacterUserAndIsActiveTrue(group, user);
        
        if (memberships.isEmpty()) {
            throw new RuntimeException("Вы не являетесь участником этой группы");
        }
        
        // Деактивируем все членства
        memberships.forEach(member -> {
            member.setIsActive(false);
            groupMemberRepository.save(member);
        });
        
        log.info("User {} left group {}", user.getUsername(), group.getName());
    }

    @Transactional
    public void deleteGroup(Long groupId, User leader) {
        Group group = getGroupById(groupId, leader);
        group.setIsActive(false);
        groupRepository.save(group);
        
        List<GroupMember> members = groupMemberRepository.findActiveMembers(group);
        members.forEach(member -> {
            member.setIsActive(false);
            groupMemberRepository.save(member);
        });
        
        log.info("Deleted group: id={}, name={}", groupId, group.getName());
    }

    @Transactional
    public void transferLeadership(Long groupId, User currentLeader, Long newLeaderId) {
        Group group = getGroupById(groupId, currentLeader);
        
        GroupMember newLeaderMember = groupMemberRepository.findById(newLeaderId)
                .orElseThrow(() -> new RuntimeException("New leader member not found: " + newLeaderId));
        
        if (!newLeaderMember.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Member is not in this group");
        }
        
        User newLeader = newLeaderMember.getCharacter().getUser();
        group.setLeader(newLeader);
        groupRepository.save(group);
        
        log.info("Transferred leadership: groupId={}, oldLeader={}, newLeader={}", 
                groupId, currentLeader.getUsername(), newLeader.getUsername());
    }

    // === Система приглашений ===

    @Transactional
    public GroupInvitation createInvitation(Long groupId, User leader) {
        Group group = getGroupByIdForLeader(groupId, leader);
        
        // Проверяем есть ли уже активное приглашение
        LocalDateTime now = LocalDateTime.now();
        var existingInvitation = groupInvitationRepository.findActiveInvitationForGroup(group, now);
        if (existingInvitation.isPresent()) {
            return existingInvitation.get();
        }
        
        // Генерируем уникальный код
        String code = generateUniqueCode();
        
        GroupInvitation invitation = GroupInvitation.builder()
                .group(group)
                .code(code)
                .createdAt(now)
                .expiresAt(now.plusMinutes(INVITATION_EXPIRY_MINUTES))
                .isUsed(false)
                .build();
        
        log.info("Created invitation: groupId={}, code={}", groupId, code);
        return groupInvitationRepository.save(invitation);
    }

    @Transactional
    public GroupMember useInvitation(String code, Character character) {
        LocalDateTime now = LocalDateTime.now();
        
        GroupInvitation invitation = groupInvitationRepository.findValidInvitation(code.toUpperCase(), now)
                .orElseThrow(() -> new RuntimeException("Код приглашения недействителен или истёк"));
        
        Group group = invitation.getGroup();
        
        // Проверяем что персонаж ещё не в группе
        groupMemberRepository.findByGroupAndCharacter(group, character)
                .ifPresent(member -> {
                    throw new RuntimeException("Персонаж уже в этой группе");
                });
        
        // Добавляем участника
        GroupMember member = GroupMember.builder()
                .group(group)
                .character(character)
                .joinedAt(now)
                .isActive(true)
                .build();
        
        // Помечаем приглашение использованным
        invitation.setIsUsed(true);
        invitation.setUsedByCharacter(character);
        invitation.setUsedAt(now);
        groupInvitationRepository.save(invitation);
        
        log.info("Used invitation: code={}, character={}, group={}", 
                code, character.getName(), group.getName());
        return groupMemberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public GroupInvitation getValidInvitation(String code) {
        return groupInvitationRepository.findValidInvitation(code.toUpperCase(), LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Код приглашения недействителен или истёк"));
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = generateCode();
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Unable to generate unique invitation code");
            }
        } while (groupInvitationRepository.existsByCode(code));
        return code;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(INVITATION_CODE_LENGTH);
        for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
            sb.append(INVITATION_CHARS.charAt(RANDOM.nextInt(INVITATION_CHARS.length())));
        }
        return sb.toString();
    }
}
