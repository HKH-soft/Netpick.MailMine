package ir.netpick.platform.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Orchestrates AI requests to the configured provider (Gemini or OpenAI-compatible).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIOrchestrationService {

    private final GeminiService geminiService;
    private final OpenAIService openAIService;

    @Value("${ai.provider:gemini}")
    private String provider;

    private AIProviderService getActiveProvider() {
        return switch (provider.toLowerCase()) {
            case "openai" -> openAIService;
            case "gemini" -> geminiService;
            default -> {
                log.warn("Unknown AI provider '{}', defaulting to gemini", provider);
                yield geminiService;
            }
        };
    }

    public String generateText(String prompt) {
        AIProviderService active = getActiveProvider();
        log.debug("Routing AI request to provider: {}", active.getProviderName());
        return active.generateText(prompt);
    }

    public String generateText(String systemInstruction, String userPrompt) {
        AIProviderService active = getActiveProvider();
        log.debug("Routing AI request to provider: {}", active.getProviderName());
        return active.generateText(systemInstruction, userPrompt);
    }

    public String generateShortText(String prompt) {
        AIProviderService active = getActiveProvider();
        log.debug("Routing AI request to provider: {}", active.getProviderName());
        return active.generateShortText(prompt);
    }

    public String getActiveProviderName() {
        return provider.toLowerCase();
    }
}