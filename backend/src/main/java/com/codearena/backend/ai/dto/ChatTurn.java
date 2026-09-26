package com.codearena.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatTurn(
        @NotBlank(message = "role is required") String role,
        @NotBlank(message = "content is required") String content) {
}
