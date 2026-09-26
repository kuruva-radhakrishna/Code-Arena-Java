package com.codearena.compiler.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

abstract class AbstractLanguageExecutor implements LanguageExecutor {

    protected final ProcessRunner processRunner;

    protected AbstractLanguageExecutor(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }

    @Override
    public final ExecutionOutcome execute(String code, String input, long timeoutMs) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("codearena-exec-");
            return doExecute(workDir, code, input, timeoutMs);
        } catch (IOException e) {
            return new ExecutionOutcome(ExecutionStatus.RUNTIME_ERROR, null,
                    "Internal error while executing code: " + e.getMessage(), 0);
        } finally {
            if (workDir != null) {
                deleteRecursively(workDir);
            }
        }
    }

    protected abstract ExecutionOutcome doExecute(Path workDir, String code, String input, long timeoutMs)
            throws IOException;

    /** Runs an optional compile step, then the run step with an enforced timeout, classifying the outcome. */
    protected final ExecutionOutcome compileAndRun(
            Path workDir, List<String> compileCommand, List<String> runCommand, String input, long timeoutMs)
            throws IOException {
        if (compileCommand != null) {
            ProcessRunner.ProcessOutput compileResult = processRunner.run(compileCommand, workDir, null, timeoutMs);
            if (compileResult.exitCode() != 0) {
                return ExecutionOutcome.compilationError(ErrorMessages.lastNonBlankLine(compileResult.stderr()));
            }
        }

        long start = System.nanoTime();
        ProcessRunner.ProcessOutput runResult = processRunner.run(runCommand, workDir, input, timeoutMs);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        if (runResult.timedOut()) {
            return ExecutionOutcome.timeLimitExceeded(timeoutMs);
        }
        if (runResult.exitCode() != 0) {
            return ExecutionOutcome.runtimeError(ErrorMessages.lastNonBlankLine(runResult.stderr()), elapsedMs);
        }
        return ExecutionOutcome.success(runResult.stdout(), elapsedMs);
    }

    private void deleteRecursively(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // Best-effort cleanup; a leftover temp file isn't worth failing the request over.
                }
            });
        } catch (IOException ignored) {
            // Directory may already be gone or inaccessible - nothing more we can do.
        }
    }
}
