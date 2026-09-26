package com.codearena.backend.user.dto;

public record AuthResponse(String token, UserResponse user) {
}
