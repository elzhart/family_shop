package com.familyshop.security;

import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    // СЕКРЕТ лучше вынести в ENV (SPRING_JWT_SECRET). Здесь fallback.
    private final Key key;

    public JwtService(org.springframework.core.env.Environment env) {
        String secret = env.getProperty("spring.jwt.secret", "dev-secret-please-change-in-prod-32bytes!!!!!");
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generate(String subject, Map<String, Object> claims, long ttlMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String getSubject(String token) {
        return parse(token).getBody().getSubject();
    }
}