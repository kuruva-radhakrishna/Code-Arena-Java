package com.codearena.backend.ai;

import java.util.List;

/**
 * Request/response shape for Google's Generative Language REST API
 * ({@code POST /v1beta/models/{model}:generateContent}), used only by
 * {@link HttpGeminiClient}.
 */
final class GeminiWireFormat {

    record Part(String text) {
    }

    record Content(List<Part> parts) {
        static Content of(String text) {
            return new Content(List.of(new Part(text)));
        }
    }

    record GenerateContentRequest(List<Content> contents) {
        static GenerateContentRequest of(String prompt) {
            return new GenerateContentRequest(List.of(Content.of(prompt)));
        }
    }

    record Candidate(Content content) {
    }

    record GenerateContentResponse(List<Candidate> candidates) {
    }

    private GeminiWireFormat() {
    }
}
