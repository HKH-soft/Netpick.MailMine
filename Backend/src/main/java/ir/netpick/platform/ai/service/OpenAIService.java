package ir.netpick.platform.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible API service supporting any provider with OpenAI-compatible endpoints.
 * Works with OpenAI, DeepSeek, Grok, Ollama, and other compatible providers.
 */
@Slf4j
@Service
public class OpenAIService implements AIProviderService {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${openai.max-prompt-length:10000}")
    private int maxPromptLength;

    private WebClient webClient;

    @Override
    public String getProviderName() {
        return "openai";
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .build();
        }
        return webClient;
    }

    @Override
    @Retryable(
            retryFor = { RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            recover = "recoverGenerateText"
    )
    public String generateText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        if (prompt.length() > maxPromptLength) {
            log.warn("Prompt exceeds max length ({}), truncating", maxPromptLength);
            prompt = prompt.substring(0, maxPromptLength);
        }

        log.debug("Attempting to generate text with OpenAI-compatible API, model: {}, timeout: {}s", model, timeoutSeconds);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 4096
        );

        try {
            return getWebClient().post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(this::extractResponseText)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
        } catch (Exception e) {
            log.error("Failed to generate text with OpenAI-compatible API: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateText(String systemInstruction, String userPrompt) {
        String fullPrompt = systemInstruction + "\n\nUser request: " + userPrompt;
        return generateText(fullPrompt);
    }

    @Override
    public String generateShortText(String prompt) {
        return generateText(prompt + "\n\nRespond in a few words only.");
    }

    private String extractResponseText(Map<String, Object> response) {
        if (response == null) {
            throw new RuntimeException("Empty response from API");
        }
        
        Object choices = response.get("choices");
        if (!(choices instanceof List<?> list) || list.isEmpty()) {
            throw new RuntimeException("No choices in API response");
        }
        
        Map<String, Object> firstChoice = (Map<String, Object>) list.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        
        return (String) message.get("content");
    }

    @Recover
    protected String recoverGenerateText(RuntimeException e, String prompt) {
        log.error("All retry attempts exhausted for OpenAI API call. Prompt: {}", 
                prompt.substring(0, Math.min(100, prompt.length())));
        throw new RuntimeException("OpenAI API call failed after retries: " + e.getMessage(), e);
    }
}