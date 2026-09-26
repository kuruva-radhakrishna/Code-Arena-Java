package com.codearena.compiler.execution;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.codearena.compiler.execution.dto.ExecuteRequest;
import com.codearena.compiler.execution.dto.ExecuteResponse;

@Service
public class CodeExecutionService {

    private final Map<Language, LanguageExecutor> executorsByLanguage;
    private final long timeoutMs;

    public CodeExecutionService(
            List<LanguageExecutor> executors,
            @Value("${app.execution.timeout-ms}") long timeoutMs) {
        this.executorsByLanguage = executors.stream()
                .collect(Collectors.toMap(LanguageExecutor::language, Function.identity()));
        this.timeoutMs = timeoutMs;
    }

    public ExecuteResponse execute(ExecuteRequest request) {
        LanguageExecutor executor = executorsByLanguage.get(request.language());
        ExecutionOutcome outcome = executor.execute(request.code(), request.input(), timeoutMs);
        return new ExecuteResponse(outcome.status(), outcome.output(), outcome.error(), outcome.executionTimeMs());
    }
}
