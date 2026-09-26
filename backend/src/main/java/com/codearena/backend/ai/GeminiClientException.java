package com.codearena.backend.ai;

public class GeminiClientException extends RuntimeException {

    public GeminiClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeminiClientException(String message) {
        super(message);
    }
}
