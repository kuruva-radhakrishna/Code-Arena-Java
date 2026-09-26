package com.codearena.backend.ai.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "message is required") String message,
        List<ChatTurn> chatHistory) {
}
