package com.codearena.backend.ai.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ContestDescriptionRequest(
        @NotBlank(message = "contestTitle is required") String contestTitle,
        @NotEmpty(message = "At least one problem is required") List<String> problemNames) {
}
