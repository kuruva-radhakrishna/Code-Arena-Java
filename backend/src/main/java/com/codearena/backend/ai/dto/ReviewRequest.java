package com.codearena.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(
        @NotBlank(message = "Code is required") String code,
        @NotBlank(message = "problemId is required") String problemId) {
}
