package com.familyshop.service;

import com.familyshop.dto.UserDto;
import com.familyshop.mapper.UserMapper;
import com.familyshop.model.Family;
import com.familyshop.model.User;
import com.familyshop.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDto registerUser(String email, String rawPassword, Family family) {
        String hash = passwordEncoder.encode(rawPassword);
        User user = User.builder()
                .email(email)
                .passwordHash(hash)
                .family(family)
                .build();
        return UserMapper.toUserDto(userRepository.save(user));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}