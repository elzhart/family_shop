package com.familyshop.dto;

import java.util.List;

public record FamilyDto(
        Long id,
        String name,
        List<UserDto> users

) {
}
