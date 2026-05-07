package com.gaming.bot.model.backup;

import com.gaming.bot.model.Equipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDto {
    
    private Long id;
    private Long characterId;
    private String itemName;
    private String itemType;
    private Integer quantity;
    private String description;
    private String imageFileId;
    private LocalDateTime addedAt;
    
    public static EquipmentDto fromEntity(Equipment equipment) {
        return EquipmentDto.builder()
                .id(equipment.getId())
                .characterId(equipment.getCharacter().getId())
                .itemName(equipment.getItemName())
                .itemType(equipment.getItemType())
                .quantity(equipment.getQuantity())
                .description(equipment.getDescription())
                .imageFileId(equipment.getImageFileId())
                .addedAt(equipment.getAddedAt())
                .build();
    }
}
