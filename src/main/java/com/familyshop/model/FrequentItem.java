package com.familyshop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// =================== 5. FrequentItem ===================
@Entity
@Table(name = "frequent_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "item_name")
    private String itemName;

    private Integer frequency = 0;
}