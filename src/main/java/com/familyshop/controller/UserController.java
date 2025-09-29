package com.familyshop.controller;

import com.familyshop.dto.UserDto;
import com.familyshop.model.Family;
import com.familyshop.model.User;
import com.familyshop.service.FamilyService;
import com.familyshop.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FamilyService familyService;

    @PostMapping("/register")
    public UserDto registerUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Long familyId
    ) {
        Family family = familyService.getFamilyById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        return userService.registerUser(email, password, family);
    }

    @GetMapping("/{email}")
    public User getByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}