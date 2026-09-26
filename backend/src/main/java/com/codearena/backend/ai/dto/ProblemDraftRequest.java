package com.codearena.backend.ai.dto;

import java.util.List;

import com.codearena.backend.problem.Difficulty;

import jakarta.validation.constraints.NotBlank;

/** Whatever the admin has already typed for a new problem; the AI fills in the rest. */
public record ProblemDraftRequest(
        @NotBlank(message = "problemName is required") String problemName,
        String description,
        Difficulty difficulty,
        List<String> topics) {
}
