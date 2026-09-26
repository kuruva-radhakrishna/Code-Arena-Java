package com.codearena.backend.compiler;

public class CompilerClientException extends RuntimeException {

    public CompilerClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerClientException(String message) {
        super(message);
    }
}
