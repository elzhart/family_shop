package com.familyshop.auth;

import com.familyshop.repository.UserRepository;
import com.familyshop.security.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public record LoginReq(String email, String password) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        var optUser = users.findByEmail(req.email());
        if (optUser.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "bad_credentials"));
        var user = optUser.get();
        if (!encoder.matches(req.password(), user.getPasswordHash()))
            return ResponseEntity.status(401).body(Map.of("error", "bad_credentials"));

        String token = jwt.generate(user.getEmail(),
                Map.of("uid", user.getId(), "familyId", user.getFamily().getId()),
                1000L * 60 * 60 * 12 // 12h
        );
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of("id", user.getId(), "email", user.getEmail(), "familyId", user.getFamily().getId())
        ));
    }
}