package com.codearena.compiler.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.codearena.compiler.execution.dto.ExecuteRequest;
import com.codearena.compiler.execution.dto.ExecuteResponse;

/**
 * Exercises the real gcc/g++/python3/javac toolchain (present on GitHub Actions'
 * ubuntu-latest runners, and baked into the compiler service's own Docker image -
 * see docs/STAGES.md for why this project doesn't run these against a local
 * JDK/toolchain install).
 */
class CodeExecutionServiceTest {

    private static final long TIMEOUT_MS = 8000;
    private static final long SHORT_TIMEOUT_MS = 500;

    private final CodeExecutionService service = new CodeExecutionService(
            List.of(new CExecutor(new ProcessRunner()), new CppExecutor(new ProcessRunner()),
                    new JavaExecutor(new ProcessRunner()), new PythonExecutor(new ProcessRunner())),
            TIMEOUT_MS);

    private ExecuteResponse run(Language language, String code, String input) {
        return service.execute(new ExecuteRequest(language, code, input));
    }

    @Test
    void c_success() {
        String code = """
                #include <stdio.h>
                int main() {
                    int a, b;
                    scanf("%d %d", &a, &b);
                    printf("%d\\n", a + b);
                    return 0;
                }
                """;

        ExecuteResponse response = run(Language.C, code, "2 3");

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.output().strip()).isEqualTo("5");
    }

    @Test
    void c_compilationError() {
        ExecuteResponse response = run(Language.C, "int main() { this is not valid C", "");

        assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
        assertThat(response.error()).isNotBlank();
    }

    @Test
    void c_runtimeError_onNonZeroExit() {
        String code = """
                int main() {
                    return 1;
                }
                """;

        ExecuteResponse response = run(Language.C, code, "");

        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
    }

    @Test
    void cpp_success() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    int a, b;
                    cin >> a >> b;
                    cout << (a + b) << endl;
                    return 0;
                }
                """;

        ExecuteResponse response = run(Language.CPP, code, "10 20");

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.output().strip()).isEqualTo("30");
    }

    @Test
    void java_success() {
        String code = """
                import java.util.Scanner;
                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int a = sc.nextInt();
                        int b = sc.nextInt();
                        System.out.println(a + b);
                    }
                }
                """;

        ExecuteResponse response = run(Language.JAVA, code, "7 8");

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.output().strip()).isEqualTo("15");
    }

    @Test
    void java_compilationError() {
        ExecuteResponse response = run(Language.JAVA, "public class Main { this is not valid java", "");

        assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
    }

    @Test
    void python_success() {
        String code = """
                a, b = map(int, input().split())
                print(a + b)
                """;

        ExecuteResponse response = run(Language.PYTHON, code, "4 5");

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.output().strip()).isEqualTo("9");
    }

    @Test
    void python_syntaxError_classifiedAsCompilationError() {
        ExecuteResponse response = run(Language.PYTHON, "def broken(:\n    pass", "");

        assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
    }

    @Test
    void python_runtimeError_onUncaughtException() {
        ExecuteResponse response = run(Language.PYTHON, "raise ValueError('boom')", "");

        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
    }

    @Test
    void python_infiniteLoop_isKilledAndReportedAsTimeLimitExceeded() {
        CodeExecutionService shortTimeoutService = new CodeExecutionService(
                List.of(new PythonExecutor(new ProcessRunner())), SHORT_TIMEOUT_MS);

        ExecuteResponse response = shortTimeoutService.execute(
                new ExecuteRequest(Language.PYTHON, "while True:\n    pass", ""));

        assertThat(response.status()).isEqualTo(ExecutionStatus.TIME_LIMIT_EXCEEDED);
    }
}
