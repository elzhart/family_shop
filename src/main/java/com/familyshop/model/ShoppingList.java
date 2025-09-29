package com.familyshop.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// =================== 3. ShoppingList ===================
@Entity
@Table(name = "shopping_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "item_name")
    private String itemName;

    private String quantity;

    @Column(name = "is_bought")
    private Boolean isBought = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ShoppingList(Long id, Family family, String itemName, String quantity, Boolean isBought) {
        this.id = id;
        this.family = family;
        this.itemName = itemName;
        this.quantity = quantity;
        this.isBought = isBought;
    }
}