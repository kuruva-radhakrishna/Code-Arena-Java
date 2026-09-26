package com.codearena.backend.compiler;

public interface CompilerClient {

    /**
     * Runs {@code request} against the compiler service.
     *
     * @throws CompilerClientException if the service is unreachable, times out,
     *     or returns an unexpected/error response.
     */
    ExecuteResponse execute(ExecuteRequest request);
}
