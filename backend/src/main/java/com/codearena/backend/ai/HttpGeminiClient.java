package com.codearena.backend.ai;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.codearena.backend.ai.GeminiWireFormat.Candidate;
import com.codearena.backend.ai.GeminiWireFormat.GenerateContentRequest;
import com.codearena.backend.ai.GeminiWireFormat.GenerateContentResponse;

@Component
public class HttpGeminiClient implements GeminiClient {

    private final RestClient restClient;
    private final String model;
    private final String apiKey;

    public HttpGeminiClient(
            @Value("${app.gemini.base-url}") String baseUrl,
            @Value("${app.gemini.model}") String model,
            @Value("${app.gemini.api-key}") String apiKey,
            @Value("${app.gemini.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.gemini.read-timeout-ms:30000}") int readTimeoutMs) {
        this.model = model;
        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String generateContent(String prompt) {
        try {
            GenerateContentResponse response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .body(GenerateContentRequest.of(prompt))
                    .retrieve()
                    .body(GenerateContentResponse.class);

            return extractText(response);
        } catch (RestClientException ex) {
            throw new GeminiClientException("Failed to reach the Gemini API", ex);
        }
    }

    private String extractText(GenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new GeminiClientException("Gemini returned no candidates");
        }
        Candidate candidate = response.candidates().get(0);
        List<GeminiWireFormat.Part> parts = candidate.content() != null ? candidate.content().parts() : null;
        if (parts == null || parts.isEmpty()) {
            throw new GeminiClientException("Gemini returned an empty response");
        }
        return parts.get(0).text();
    }
}
