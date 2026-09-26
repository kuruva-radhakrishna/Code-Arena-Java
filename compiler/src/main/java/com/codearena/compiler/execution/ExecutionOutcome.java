package com.codearena.compiler.execution;

public record ExecutionOutcome(ExecutionStatus status, String output, String error, long executionTimeMs) {

    public static ExecutionOutcome success(String output, long executionTimeMs) {
        return new ExecutionOutcome(ExecutionStatus.SUCCESS, output, null, executionTimeMs);
    }

    public static ExecutionOutcome compilationError(String error) {
        return new ExecutionOutcome(ExecutionStatus.COMPILATION_ERROR, null, error, 0);
    }

    public static ExecutionOutcome runtimeError(String error, long executionTimeMs) {
        return new ExecutionOutcome(ExecutionStatus.RUNTIME_ERROR, null, error, executionTimeMs);
    }

    public static ExecutionOutcome timeLimitExceeded(long timeoutMs) {
        return new ExecutionOutcome(ExecutionStatus.TIME_LIMIT_EXCEEDED, null, "Execution exceeded the time limit", timeoutMs);
    }
}
