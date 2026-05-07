package com.gaming.bot.service;

import com.gaming.bot.model.Character;
import com.gaming.bot.model.Wishlist;
import com.gaming.bot.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    @Transactional
    public Wishlist addWish(Character character, String itemName, String itemType, 
                          Integer priority, String notes) {
        Wishlist wishlist = Wishlist.builder()
                .character(character)
                .itemName(itemName)
                .itemType(itemType)
                .priority(priority != null ? priority : 1)
                .notes(notes)
                .addedAt(LocalDateTime.now())
                .isFulfilled(false)
                .build();
        
        log.info("Adding wish: item={}, character={}", itemName, character.getName());
        return wishlistRepository.save(wishlist);
    }

    @Transactional(readOnly = true)
    public List<Wishlist> getCharacterWishes(Character character) {
        return wishlistRepository.findByCharacterOrderByPriorityDescAddedAtAsc(character);
    }

    @Transactional
    public void fulfillWish(Long wishId) {
        Wishlist wish = wishlistRepository.findById(wishId)
                .orElseThrow(() -> new RuntimeException("Wish not found: " + wishId));
        wish.setIsFulfilled(true);
        wishlistRepository.save(wish);
        log.info("Fulfilled wish: id={}, item={}", wishId, wish.getItemName());
    }

    @Transactional
    public void removeWish(Long wishId) {
        wishlistRepository.deleteById(wishId);
        log.info("Removed wish: id={}", wishId);
    }
    
    @Transactional(readOnly = true)
    public Wishlist getWishById(Long wishId) {
        return wishlistRepository.findById(wishId)
                .orElseThrow(() -> new RuntimeException("Wish not found: " + wishId));
    }
    
    @Transactional
    public Wishlist updateWish(Long wishId, String itemName, String itemType, Integer priority, String notes) {
        Wishlist wish = getWishById(wishId);
        wish.setItemName(itemName);
        wish.setItemType(itemType);
        wish.setPriority(priority);
        wish.setNotes(notes);
        log.info("Updated wish: id={}, item={}", wishId, itemName);
        return wishlistRepository.save(wish);
    }
    
    @Transactional
    public void deleteWish(Long wishId) {
        wishlistRepository.deleteById(wishId);
        log.info("Deleted wish: id={}", wishId);
    }
}
