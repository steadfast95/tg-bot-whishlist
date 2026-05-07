package com.gaming.bot.repository;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Group;
import com.gaming.bot.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    List<GroupMember> findByGroupAndIsActiveTrue(Group group);
    
    Optional<GroupMember> findByGroupAndCharacter(Group group, Character character);
    
    @Query("SELECT gm FROM GroupMember gm " +
           "JOIN FETCH gm.character c " +
           "JOIN FETCH c.user " +
           "WHERE gm.group = :group AND gm.isActive = true")
    List<GroupMember> findActiveMembers(@Param("group") Group group);
    
    @Query("SELECT DISTINCT gm.group FROM GroupMember gm " +
           "WHERE gm.character.user = :user AND gm.isActive = true AND gm.group.isActive = true")
    List<Group> findGroupsByUserCharacters(@Param("user") com.gaming.bot.model.User user);
    
    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END FROM GroupMember gm " +
           "WHERE gm.group = :group AND gm.character.user = :user AND gm.isActive = true")
    boolean existsByGroupAndCharacterUser(@Param("group") Group group, @Param("user") com.gaming.bot.model.User user);
    
    @Query("SELECT gm FROM GroupMember gm " +
           "JOIN FETCH gm.character c " +
           "WHERE gm.group = :group AND c.user = :user AND gm.isActive = true")
    List<GroupMember> findByGroupAndCharacterUserAndIsActiveTrue(@Param("group") Group group, @Param("user") com.gaming.bot.model.User user);
}
