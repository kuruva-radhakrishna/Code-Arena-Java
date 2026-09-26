package com.codearena.compiler.execution;

public interface LanguageExecutor {

    Language language();

    /**
     * Compiles (if applicable) and runs {@code code} against {@code input}, within
     * its own isolated temp workspace, enforcing {@code timeoutMs} on the run step.
     */
    ExecutionOutcome execute(String code, String input, long timeoutMs);
}
