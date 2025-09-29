package com.familyshop.dto;

public record UserDto(
        Long id,
        String email,
        Long groupId
) {
}
