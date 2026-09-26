package com.codearena.compiler.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CExecutor extends AbstractLanguageExecutor {

    public CExecutor(ProcessRunner processRunner) {
        super(processRunner);
    }

    @Override
    public Language language() {
        return Language.C;
    }

    @Override
    protected ExecutionOutcome doExecute(Path workDir, String code, String input, long timeoutMs) throws IOException {
        Path source = workDir.resolve("main.c");
        Files.writeString(source, code);
        Path binary = workDir.resolve("main");

        return compileAndRun(
                workDir,
                List.of("gcc", source.toString(), "-o", binary.toString()),
                List.of(binary.toString()),
                input,
                timeoutMs);
    }
}
