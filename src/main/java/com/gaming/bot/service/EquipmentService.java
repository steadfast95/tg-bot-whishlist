package com.gaming.bot.service;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Equipment;
import com.gaming.bot.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    @Transactional
    public Equipment addEquipment(Character character, String itemName, String itemType, 
                                Integer quantity, String description) {
        Equipment equipment = Equipment.builder()
                .character(character)
                .itemName(itemName)
                .itemType(itemType)
                .quantity(quantity != null ? quantity : 1)
                .description(description)
                .addedAt(LocalDateTime.now())
                .build();
        
        log.info("Adding equipment: item={}, character={}", itemName, character.getName());
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment addEquipmentWithImage(Character character, String itemName, String itemType, 
                                         Integer quantity, String description, String imageFileId) {
        Equipment equipment = Equipment.builder()
                .character(character)
                .itemName(itemName)
                .itemType(itemType)
                .quantity(quantity != null ? quantity : 1)
                .description(description)
                .imageFileId(imageFileId)
                .addedAt(LocalDateTime.now())
                .build();
        
        log.info("Adding equipment with image: item={}, character={}, imageFileId={}", 
                itemName, character.getName(), imageFileId);
        return equipmentRepository.save(equipment);
    }

    @Transactional(readOnly = true)
    public List<Equipment> getCharacterEquipment(Character character) {
        return equipmentRepository.findByCharacterOrderByAddedAtDesc(character);
    }

    @Transactional
    public void removeEquipment(Long equipmentId) {
        equipmentRepository.deleteById(equipmentId);
        log.info("Removed equipment: id={}", equipmentId);
    }
}
