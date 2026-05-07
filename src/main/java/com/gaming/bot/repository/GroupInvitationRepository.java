package com.gaming.bot.repository;

import com.gaming.bot.model.Group;
import com.gaming.bot.model.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    Optional<GroupInvitation> findByCodeAndIsUsedFalse(String code);
    
    @Query("SELECT gi FROM GroupInvitation gi " +
           "JOIN FETCH gi.group g " +
           "WHERE gi.code = :code AND gi.isUsed = false AND gi.expiresAt > :now AND g.isActive = true")
    Optional<GroupInvitation> findValidInvitation(@Param("code") String code, @Param("now") LocalDateTime now);
    
    @Query("SELECT gi FROM GroupInvitation gi " +
           "WHERE gi.group = :group AND gi.isUsed = false AND gi.expiresAt > :now")
    Optional<GroupInvitation> findActiveInvitationForGroup(@Param("group") Group group, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM GroupInvitation gi WHERE gi.expiresAt < :now AND gi.isUsed = false")
    void deleteExpiredInvitations(@Param("now") LocalDateTime now);
    
    boolean existsByCode(String code);
}
