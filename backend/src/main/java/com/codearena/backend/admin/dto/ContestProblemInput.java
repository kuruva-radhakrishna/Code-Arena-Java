package com.codearena.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ContestProblemInput(
        @NotBlank(message = "problemId is required") String problemId,
        /** Defaults to 4 if not provided, matching the original schema's default. */
        Integer points) {
}
