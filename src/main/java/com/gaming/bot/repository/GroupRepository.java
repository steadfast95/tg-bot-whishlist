package com.gaming.bot.repository;

import com.gaming.bot.model.Group;
import com.gaming.bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    @Query("SELECT g FROM Group g JOIN FETCH g.leader WHERE g.leader = :leader AND g.isActive = true")
    List<Group> findByLeaderAndIsActiveTrue(@Param("leader") User leader);
    
    @Query("SELECT g FROM Group g JOIN FETCH g.leader WHERE g.id = :id AND g.leader = :leader")
    Optional<Group> findByIdAndLeader(@Param("id") Long id, @Param("leader") User leader);
    
    List<Group> findByLeader(User leader);
    
    @Query("SELECT g FROM Group g JOIN FETCH g.leader WHERE g.id = :id AND g.isActive = true")
    Optional<Group> findByIdAndIsActiveTrue(@Param("id") Long id);
}
