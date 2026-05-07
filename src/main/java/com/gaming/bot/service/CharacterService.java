package com.gaming.bot.service;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.User;
import com.gaming.bot.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;

    @Transactional
    public Character createCharacter(User user, String name, String characterClass, Integer level, String description) {
        Character character = Character.builder()
                .user(user)
                .name(name)
                .characterClass(characterClass)
                .level(level)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        
        log.info("Creating character: name={}, class={}, user={}", name, characterClass, user.getUsername());
        return characterRepository.save(character);
    }

    @Transactional(readOnly = true)
    public List<Character> getUserCharacters(User user) {
        return characterRepository.findByUserAndIsActiveTrue(user);
    }

    @Transactional(readOnly = true)
    public Character getCharacterById(Long characterId, User user) {
        return characterRepository.findByIdAndUser(characterId, user)
                .orElseThrow(() -> new RuntimeException("Character not found: " + characterId));
    }

    @Transactional(readOnly = true)
    public Character getCharacterByIdAnyUser(Long characterId) {
        return characterRepository.findByIdWithUser(characterId)
                .filter(Character::getIsActive)
                .orElseThrow(() -> new RuntimeException("Character not found: " + characterId));
    }

    @Transactional
    public Character updateCharacter(Long characterId, User user, String name, String characterClass, 
                                   Integer level, String description) {
        Character character = getCharacterById(characterId, user);
        character.setName(name);
        character.setCharacterClass(characterClass);
        character.setLevel(level);
        character.setDescription(description);
        character.setUpdatedAt(LocalDateTime.now());
        
        log.info("Updating character: id={}, name={}", characterId, name);
        return characterRepository.save(character);
    }

    @Transactional
    public void deleteCharacter(Long characterId, User user) {
        Character character = getCharacterById(characterId, user);
        character.setIsActive(false);
        characterRepository.save(character);
        log.info("Deleted character: id={}, name={}", characterId, character.getName());
    }
}
