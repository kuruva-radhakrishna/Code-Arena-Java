package com.codearena.compiler.execution.dto;

import com.codearena.compiler.execution.ExecutionStatus;

public record ExecuteResponse(ExecutionStatus status, String output, String error, long executionTimeMs) {
}
