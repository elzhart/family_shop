package com.familyshop.mapper;

import com.familyshop.dto.UserDto;
import com.familyshop.model.User;

public class UserMapper {

    public static UserDto toUserDto(User user) {

        return new UserDto(user.getId(), user.getEmail(), user.getFamily().getId());

    }
}