package com.codearena.compiler.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Submitted code must define {@code public class Main} - each execution runs in
 * its own temp directory, so (unlike the original, which had to generate a
 * unique class name per submission to avoid collisions on a shared folder)
 * there's no need to rewrite the class name.
 */
@Component
public class JavaExecutor extends AbstractLanguageExecutor {

    public JavaExecutor(ProcessRunner processRunner) {
        super(processRunner);
    }

    @Override
    public Language language() {
        return Language.JAVA;
    }

    @Override
    protected ExecutionOutcome doExecute(Path workDir, String code, String input, long timeoutMs) throws IOException {
        Path source = workDir.resolve("Main.java");
        Files.writeString(source, code);

        return compileAndRun(
                workDir,
                List.of("javac", source.toString()),
                List.of("java", "-cp", workDir.toString(), "Main"),
                input,
                timeoutMs);
    }
}
