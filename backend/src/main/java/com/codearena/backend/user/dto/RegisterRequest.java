package com.codearena.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 2, message = "First name must be at least 2 characters")
        String firstname,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, message = "Last name must be at least 2 characters")
        String lastname,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password) {
}
