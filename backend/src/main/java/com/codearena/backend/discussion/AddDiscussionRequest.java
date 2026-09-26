package com.codearena.backend.discussion;

import jakarta.validation.constraints.NotBlank;

public record AddDiscussionRequest(@NotBlank(message = "Comment is required") String comment) {
}
