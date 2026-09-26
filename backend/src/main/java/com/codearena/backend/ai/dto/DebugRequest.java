package com.codearena.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record DebugRequest(
        @NotBlank(message = "Code is required") String code,
        String problemDescription) {
}
