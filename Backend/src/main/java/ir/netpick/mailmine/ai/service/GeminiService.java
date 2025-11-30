package ir.netpick.mailmine.ai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    /**
     * Generate text from a prompt using Gemini
     */
    public String generateText(String prompt) {
        try (Client client = new Client()) {
            GenerateContentResponse response = client.models.generateContent(model, prompt, null);
            return response.text();
        } catch (Exception e) {
            log.error("Failed to generate text with Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generate text with a system instruction
     */
    public String generateText(String systemInstruction, String userPrompt) {
        String fullPrompt = systemInstruction + "\n\nUser request: " + userPrompt;
        return generateText(fullPrompt);
    }

    /**
     * Generate a short response (few words)
     */
    public String generateShortText(String prompt) {
        return generateText(prompt + "\n\nRespond in a few words only.");
    }
}
