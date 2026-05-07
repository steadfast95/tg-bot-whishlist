package com.gaming.bot.model.backup;

import com.gaming.bot.model.Wishlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDto {
    
    private Long id;
    private Long characterId;
    private String itemName;
    private String itemType;
    private Integer priority;
    private String notes;
    private LocalDateTime addedAt;
    private Boolean isFulfilled;
    
    public static WishlistDto fromEntity(Wishlist wishlist) {
        return WishlistDto.builder()
                .id(wishlist.getId())
                .characterId(wishlist.getCharacter().getId())
                .itemName(wishlist.getItemName())
                .itemType(wishlist.getItemType())
                .priority(wishlist.getPriority())
                .notes(wishlist.getNotes())
                .addedAt(wishlist.getAddedAt())
                .isFulfilled(wishlist.getIsFulfilled())
                .build();
    }
}
