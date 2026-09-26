package com.codearena.compiler.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CppExecutor extends AbstractLanguageExecutor {

    public CppExecutor(ProcessRunner processRunner) {
        super(processRunner);
    }

    @Override
    public Language language() {
        return Language.CPP;
    }

    @Override
    protected ExecutionOutcome doExecute(Path workDir, String code, String input, long timeoutMs) throws IOException {
        Path source = workDir.resolve("main.cpp");
        Files.writeString(source, code);
        Path binary = workDir.resolve("main");

        return compileAndRun(
                workDir,
                List.of("g++", source.toString(), "-o", binary.toString()),
                List.of(binary.toString()),
                input,
                timeoutMs);
    }
}
