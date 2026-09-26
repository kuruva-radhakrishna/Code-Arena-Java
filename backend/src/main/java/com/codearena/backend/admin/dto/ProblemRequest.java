package com.codearena.backend.admin.dto;

import java.util.List;

import com.codearena.backend.problem.Difficulty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProblemRequest(
        @NotBlank(message = "problemName is required")
        @Size(min = 5, message = "problemName must be at least 5 characters") String problemName,

        @NotBlank(message = "description is required")
        @Size(min = 15, message = "description must be at least 15 characters") String description,

        @NotEmpty(message = "At least one constraint is required") List<@NotBlank String> constraints,

        @NotEmpty(message = "At least one test case is required")
        List<@Valid TestCaseInput> testCases,

        @NotNull(message = "difficulty is required") Difficulty difficulty,

        @NotEmpty(message = "At least one topic is required") List<String> topics,

        List<String> hints) {
}
