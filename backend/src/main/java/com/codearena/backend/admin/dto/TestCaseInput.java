package com.codearena.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record TestCaseInput(
        @NotBlank(message = "Test case input is required") String input,
        @NotBlank(message = "Test case output is required") String output,
        boolean isPublic) {
}
