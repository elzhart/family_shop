package com.familyshop.mapper;

import com.familyshop.dto.PurchaseHistoryDto;
import com.familyshop.model.PurchaseHistory;

public class PurchaseHistoryMapper {

    public static PurchaseHistoryDto toDto(PurchaseHistory purchaseHistory) {

        return new PurchaseHistoryDto(
                FamilyMapper.toFamilyDto(purchaseHistory.getFamily()),
                purchaseHistory.getItemName(),
                purchaseHistory.getQuantity(),
                purchaseHistory.getBoughtAt()
        );

    }
}
