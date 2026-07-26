package ir.netpick.platform.ai.service;

/**
 * Common interface for AI text generation providers.
 */
public interface AIProviderService {
    String getProviderName();
    String generateText(String prompt);
    String generateText(String systemInstruction, String userPrompt);
    String generateShortText(String prompt);
}