package com.codearena.backend.admin.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContestRequest(
        @NotBlank(message = "contestTitle is required")
        @Size(min = 5, message = "contestTitle must be at least 5 characters") String contestTitle,

        @NotBlank(message = "description is required")
        @Size(min = 10, message = "description must be at least 10 characters") String description,

        @NotNull(message = "startTime is required") Instant startTime,

        @NotNull(message = "endTime is required") Instant endTime,

        @Size(min = 3, message = "A contest needs at least 3 problems")
        List<@Valid ContestProblemInput> problems) {
}
