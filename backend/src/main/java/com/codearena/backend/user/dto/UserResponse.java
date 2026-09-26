package com.codearena.backend.user.dto;

import java.time.Instant;

import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;

public record UserResponse(String id, String firstname, String lastname, String email, Role role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getRole(),
                user.getCreatedAt());
    }
}
