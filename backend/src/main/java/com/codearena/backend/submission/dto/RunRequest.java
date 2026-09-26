package com.codearena.backend.submission.dto;

import com.codearena.backend.submission.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RunRequest(
        @NotNull(message = "Language is required") Language language,
        @NotBlank(message = "Code is required") String code,
        String input) {
}
