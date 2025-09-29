package com.familyshop.mapper;


import com.familyshop.dto.FrequentItemDto;
import com.familyshop.model.FrequentItem;

public class FrequentItemMapper {

    public static FrequentItemDto toDto(FrequentItem frequentItem) {
        return new FrequentItemDto(
                frequentItem.getId(),
                FamilyMapper.toFamilyDto(frequentItem.getFamily()),
                frequentItem.getItemName(),
                frequentItem.getFrequency()
        );
    }
}
