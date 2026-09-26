package com.codearena.compiler.execution.dto;

import com.codearena.compiler.execution.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExecuteRequest(
        @NotNull(message = "language is required") Language language,
        @NotBlank(message = "code is required") String code,
        String input) {
}
