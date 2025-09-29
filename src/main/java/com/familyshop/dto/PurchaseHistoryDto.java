package com.familyshop.dto;

import java.time.LocalDateTime;

public record PurchaseHistoryDto(
        FamilyDto family,
        String itemName,
        String quantity,
        LocalDateTime boughtAt
) {
}
