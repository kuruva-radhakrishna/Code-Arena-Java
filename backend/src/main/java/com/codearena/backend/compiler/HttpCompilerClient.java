package com.codearena.backend.compiler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpCompilerClient implements CompilerClient {

    private final RestClient restClient;

    public HttpCompilerClient(
            @Value("${app.compiler.base-url}") String baseUrl,
            @Value("${app.compiler.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.compiler.read-timeout-ms:15000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        try {
            return restClient.post()
                    .uri("/api/execute")
                    .body(request)
                    .retrieve()
                    .body(ExecuteResponse.class);
        } catch (RestClientException ex) {
            throw new CompilerClientException("Failed to reach the compiler service", ex);
        }
    }
}
