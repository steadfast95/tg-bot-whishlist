package com.gaming.bot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(nullable = false)
    private String itemName;

    @Column
    private String itemType;

    @Column
    private Integer priority;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    @Column(nullable = false)
    private Boolean isFulfilled;
}
