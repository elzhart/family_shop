package com.familyshop.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// =================== 4. PurchaseHistory ===================
@Entity
@Table(name = "purchases_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "item_name")
    private String itemName;

    private String quantity;

    @Column(name = "bought_at")
    private LocalDateTime boughtAt = LocalDateTime.now();
}