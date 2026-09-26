package com.codearena.compiler.execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * Runs an external process with an enforced wall-clock timeout, concurrently
 * draining stdout/stderr while writing stdin on background threads to avoid the
 * classic ProcessBuilder deadlock (child blocks writing to a full stdout pipe
 * while we're still blocked writing stdin). The original app's compiler service
 * had no timeout at all - an infinite loop in submitted code would hang forever.
 */
@Component
public class ProcessRunner {

    private static final long STREAM_DRAIN_GRACE_MS = 2000;

    // Daemon threads: this is instantiated directly in plain unit tests too (not
    // just as a Spring bean), so it must never keep a JVM alive on its own.
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "process-runner-io");
        thread.setDaemon(true);
        return thread;
    });

    public ProcessOutput run(List<String> command, Path workingDir, String stdin, long timeoutMs) throws IOException {
        Process process = new ProcessBuilder(command).directory(workingDir.toFile()).start();

        Future<String> stdoutFuture = ioExecutor.submit(readAll(process.getInputStream()));
        Future<String> stderrFuture = ioExecutor.submit(readAll(process.getErrorStream()));
        ioExecutor.submit(() -> writeStdin(process, stdin));

        boolean finished;
        try {
            finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("Interrupted while waiting for the process to finish", e);
        }

        if (!finished) {
            process.destroyForcibly();
            stdoutFuture.cancel(true);
            stderrFuture.cancel(true);
            return new ProcessOutput(-1, "", "", true);
        }

        return new ProcessOutput(process.exitValue(), safeGet(stdoutFuture), safeGet(stderrFuture), false);
    }

    private void writeStdin(Process process, String stdin) {
        try (OutputStream out = process.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                out.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
            // The process may have already exited and closed its stdin - nothing to do.
        }
    }

    private Callable<String> readAll(InputStream in) {
        return () -> {
            try {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        };
    }

    private String safeGet(Future<String> future) {
        try {
            return future.get(STREAM_DRAIN_GRACE_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return "";
        }
    }

    @PreDestroy
    void shutdown() {
        ioExecutor.shutdownNow();
    }

    public record ProcessOutput(int exitCode, String stdout, String stderr, boolean timedOut) {
    }
}
