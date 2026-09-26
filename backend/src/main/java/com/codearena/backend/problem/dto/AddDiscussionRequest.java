package com.codearena.backend.problem.dto;

import jakarta.validation.constraints.NotBlank;

public record AddDiscussionRequest(@NotBlank(message = "Comment is required") String comment) {
}
