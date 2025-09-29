package com.familyshop.mapper;

import com.familyshop.dto.FamilyDto;
import com.familyshop.model.Family;

public class FamilyMapper {

    public static FamilyDto toFamilyDto(Family family) {
        return new FamilyDto(
                family.getId(),
                family.getName(),
                family.getUsers().stream().map(UserMapper::toUserDto).toList()
        );
    }
}
