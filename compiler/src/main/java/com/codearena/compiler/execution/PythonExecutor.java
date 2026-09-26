package com.codearena.compiler.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * No separate compile step - a Python {@code SyntaxError} is classified as a
 * COMPILATION_ERROR (matching the original's intent: a judge conventionally
 * reports "won't even run" failures as compilation errors regardless of
 * whether the language actually has a distinct compile phase).
 */
@Component
public class PythonExecutor extends AbstractLanguageExecutor {

    public PythonExecutor(ProcessRunner processRunner) {
        super(processRunner);
    }

    @Override
    public Language language() {
        return Language.PYTHON;
    }

    @Override
    protected ExecutionOutcome doExecute(Path workDir, String code, String input, long timeoutMs) throws IOException {
        Path source = workDir.resolve("main.py");
        Files.writeString(source, code);

        long start = System.nanoTime();
        ProcessRunner.ProcessOutput result = processRunner.run(
                List.of("python3", source.toString()), workDir, input, timeoutMs);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        if (result.timedOut()) {
            return ExecutionOutcome.timeLimitExceeded(timeoutMs);
        }
        if (result.exitCode() != 0) {
            String message = ErrorMessages.lastNonBlankLine(result.stderr());
            if (result.stderr() != null && result.stderr().contains("SyntaxError")) {
                return ExecutionOutcome.compilationError(message);
            }
            return ExecutionOutcome.runtimeError(message, elapsedMs);
        }
        return ExecutionOutcome.success(result.stdout(), elapsedMs);
    }
}
