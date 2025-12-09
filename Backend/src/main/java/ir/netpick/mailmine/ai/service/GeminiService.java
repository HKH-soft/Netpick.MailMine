package ir.netpick.mailmine.ai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    /**
     * Generate text from a prompt using Gemini with retry logic
     * Retries up to 3 times with exponential backoff on RuntimeException
     */
    @Retryable(
            retryFor = { RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            recover = "recoverGenerateText"
    )
    public String generateText(String prompt) {
        try (Client client = new Client()) {
            log.debug("Attempting to generate text with Gemini API");
            GenerateContentResponse response = client.models.generateContent(model, prompt, null);
            return response.text();
        } catch (Exception e) {
            Throwable rootCause = getRootCause(e);
            if (rootCause instanceof UnknownHostException unknownHost) {
                String host = unknownHost.getMessage();
                String message = "Unable to resolve Gemini API host '" + host
                        + "'. Verify internet access or configure a proxy/alternative provider.";
                log.error(message, e);
                throw new IllegalStateException(message, e);
            }

            log.error("Failed to generate text with Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback method when all retry attempts fail
     */
    @Recover
    public String recoverGenerateText(RuntimeException e, String prompt) {
        log.error("All retry attempts exhausted for Gemini API call. Prompt: {}", 
                prompt.substring(0, Math.min(100, prompt.length())));
        throw new RuntimeException("Gemini API call failed after retries: " + e.getMessage(), e);
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

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
