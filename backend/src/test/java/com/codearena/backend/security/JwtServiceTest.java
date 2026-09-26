package com.codearena.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-used-only-in-unit-tests-0123456789ABCDEF";

    private User sampleUser() {
        return User.builder()
                .id("user-123")
                .firstname("Ada")
                .lastname("Lovelace")
                .email("ada@example.com")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void generateToken_thenParseClaims_roundTripsUserInfo() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        User user = sampleUser();

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertThat(jwtService.extractUserId(claims)).isEqualTo(user.getId());
        assertThat(jwtService.extractEmail(claims)).isEqualTo(user.getEmail());
        assertThat(jwtService.extractRole(claims)).isEqualTo(Role.ADMIN);
    }

    @Test
    void isValid_returnsTrue_forFreshlyIssuedToken() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        String token = jwtService.generateToken(sampleUser());

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalse_forGarbageToken() {
        JwtService jwtService = new JwtService(SECRET, 60_000);

        assertThat(jwtService.isValid("not-a-real-jwt")).isFalse();
    }

    @Test
    void isValid_returnsFalse_forExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1);
        String token = jwtService.generateToken(sampleUser());

        Thread.sleep(20);

        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    void parseClaims_throws_whenSignedWithDifferentSecret() {
        JwtService issuer = new JwtService(SECRET, 60_000);
        JwtService verifier = new JwtService("a-completely-different-secret-0123456789ABCDEF", 60_000);
        String token = issuer.generateToken(sampleUser());

        assertThatThrownBy(() -> verifier.parseClaims(token)).isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }

    @Test
    void parseClaims_throws_forExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1);
        String token = jwtService.generateToken(sampleUser());

        Thread.sleep(20);

        assertThatThrownBy(() -> jwtService.parseClaims(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
