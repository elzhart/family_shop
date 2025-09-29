package com.familyshop.dto;

public record ShoppingListDto(
        Long id,
        FamilyDto family,
        String itemName,
        String quantity,
        Boolean isBought
) {
}
