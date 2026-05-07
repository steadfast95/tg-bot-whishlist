package com.gaming.bot.repository;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    List<Equipment> findByCharacter(Character character);
    
    @Query("SELECT e FROM Equipment e JOIN FETCH e.character WHERE e.character = :character ORDER BY e.addedAt DESC")
    List<Equipment> findByCharacterOrderByAddedAtDesc(@Param("character") Character character);
}
