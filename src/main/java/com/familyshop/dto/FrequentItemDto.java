package com.familyshop.dto;

public record FrequentItemDto(
        Long id,
        FamilyDto family,
        String itemName,
        Integer frequency
) {
}
