# Compiler service

Spring Boot 4 / Java 21 service that compiles and runs untrusted, submitted C/C++/Java/Python code against a
single input and returns its output — the execution backend the `backend` module's `CompilerClient` talks to.

## Configuration

| Env var | Purpose | Local default |
|---|---|---|
| `PORT` | HTTP port | `8000` |
| `EXECUTION_TIMEOUT_MS` | Wall-clock limit enforced on the run step of any submitted code | `10000` |

## API

- `POST /api/execute` — `{language, code, input}` → `200 {status, output, error, executionTimeMs}` for any
  well-formed request. `status` is one of `SUCCESS`, `COMPILATION_ERROR`, `RUNTIME_ERROR`,
  `TIME_LIMIT_EXCEEDED`. `400` only for a malformed request (missing/blank code, unrecognized `language`) — the
  original had no response at all for an unrecognized language (the request just hung).

## How execution works

Each request runs in its own temp directory (deleted afterward), so concurrent submissions never collide —
unlike the original, which wrote every submission's files into one shared `codes/`/`inputs/` folder and had to
generate a unique Java class name per submission to avoid clashes. Because each execution here already gets an
isolated directory, submitted Java code just needs to define `public class Main` directly.

`ProcessRunner` enforces `EXECUTION_TIMEOUT_MS` on the run step and drains stdout/stderr on background threads
while writing stdin, avoiding the classic `ProcessBuilder` deadlock (a full stdout pipe blocking the child while
the parent is still blocked writing stdin) — the original had no execution timeout at all.

Requires `gcc`, `g++`, `python3`, and a JDK (`javac`/`java`) on `PATH` — declared in this module's Dockerfile
(added in Stage 9) and, for CI, already present on GitHub Actions' `ubuntu-latest` runners, so
`CodeExecutionServiceTest` exercises the real toolchain rather than mocking it.

## Local memory limits

Not enforced per-execution in this module — the container this service runs in gets an overall memory cap at
the deployment level (Stage 9/10) instead. The original declared a `pidusage` dependency for this but never
actually used it.
