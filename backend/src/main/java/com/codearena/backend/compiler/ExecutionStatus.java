package com.codearena.backend.compiler;

/**
 * Outcome of a single code execution against one input, as reported by the
 * compiler service. Always HTTP 200 for a well-formed request — these are
 * expected outcomes of running arbitrary user code, not server errors. The
 * compiler service only returns a non-2xx status for a malformed request
 * (missing code, unrecognized language) or a genuine internal failure,
 * fixing the original app's compiler service, which had no response at all
 * for an unrecognized language.
 */
public enum ExecutionStatus {
    SUCCESS,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED
}
