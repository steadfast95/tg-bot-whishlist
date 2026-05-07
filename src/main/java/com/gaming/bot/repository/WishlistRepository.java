package com.gaming.bot.repository;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    List<Wishlist> findByCharacterAndIsFulfilledFalse(Character character);
    
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.character WHERE w.character = :character ORDER BY w.priority DESC, w.addedAt ASC")
    List<Wishlist> findByCharacterOrderByPriorityDescAddedAtAsc(@Param("character") Character character);
}
