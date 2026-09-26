package com.codearena.backend.compiler;

public record ExecuteResponse(ExecutionStatus status, String output, String error, long executionTimeMs) {
}
