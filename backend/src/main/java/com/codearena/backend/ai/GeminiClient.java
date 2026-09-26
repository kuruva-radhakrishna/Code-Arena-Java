package com.codearena.backend.ai;

public interface GeminiClient {

    /**
     * Sends {@code prompt} to Gemini and returns the model's text response.
     *
     * @throws GeminiClientException if the service is unreachable, times out, or
     *     returns an unexpected/error response.
     */
    String generateContent(String prompt);
}
