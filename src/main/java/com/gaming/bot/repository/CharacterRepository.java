package com.gaming.bot.repository;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    @Query("SELECT c FROM Character c JOIN FETCH c.user WHERE c.user = :user AND c.isActive = true")
    List<Character> findByUserAndIsActiveTrue(@Param("user") User user);
    
    @Query("SELECT c FROM Character c JOIN FETCH c.user WHERE c.id = :id AND c.user = :user")
    Optional<Character> findByIdAndUser(@Param("id") Long id, @Param("user") User user);
    
    @Query("SELECT c FROM Character c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Character> findByIdWithUser(@Param("id") Long id);
    
    List<Character> findByUser(User user);
}
