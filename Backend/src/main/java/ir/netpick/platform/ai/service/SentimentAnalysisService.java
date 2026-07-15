package ir.netpick.platform.ai.service;

import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.mailmine.model.EmailMessage;
import ir.netpick.platform.mailmine.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentAnalysisService {

    private final GeminiService geminiService;
    private final EmailMessageRepository emailMessageRepository;

    private static final String SENTIMENT_PROMPT = """
        Analyze the sentiment of this email. Return ONLY a JSON object with these fields:
        {
            "sentiment": "positive" | "neutral" | "negative" | "angry",
            "confidence": 0.0-1.0,
            "urgency": "low" | "medium" | "high" | "critical",
            "emotion": "one word describing the primary emotion",
            "key_phrases": ["list", "of", "important", "phrases"]
        }
        
        Email from: %s
        Subject: %s
        Body: %s
        """;

    /**
     * Analyze sentiment of a single email
     */
    @Async
    public CompletableFuture<Map<String, Object>> analyzeEmail(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        String prompt = String.format(SENTIMENT_PROMPT,
                sanitizeForPrompt(email.getSenderEmail()),
                sanitizeForPrompt(email.getSubject()),
                sanitizeForPrompt(email.getBodyText() != null ? truncate(email.getBodyText(), 2000) : "No content"));

        String response = geminiService.generateText(prompt);
        Map<String, Object> result = parseSentimentResponse(response);
        result.put("emailId", emailId.toString());
        result.put("sender", email.getSenderEmail());
        result.put("subject", email.getSubject());

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Analyze sentiment trend for a thread
     */
    @Async
    public CompletableFuture<Map<String, Object>> analyzeThreadTrend(String threadId) {
        List<EmailMessage> thread = emailMessageRepository
                .findByThreadIdOrderByReceivedAtAsc(threadId);

        if (thread.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of("error", "No emails found"));
        }

        List<Map<String, Object>> emailSentiments = new ArrayList<>();
        for (EmailMessage email : thread) {
            Map<String, Object> sentiment = analyzeEmail(email.getId()).join();
            sentiment.put("date", email.getReceivedAt().toString());
            emailSentiments.add(sentiment);
        }

        // Calculate trend
        long positiveCount = emailSentiments.stream()
                .filter(s -> "positive".equals(s.get("sentiment")))
                .count();
        long negativeCount = emailSentiments.stream()
                .filter(s -> "negative".equals(s.get("sentiment")) || "angry".equals(s.get("sentiment")))
                .count();

        String trend = positiveCount > negativeCount ? "improving"
                : negativeCount > positiveCount ? "declining"
                : "stable";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("threadId", threadId);
        result.put("emailCount", thread.size());
        result.put("trend", trend);
        result.put("positiveCount", positiveCount);
        result.put("negativeCount", negativeCount);
        result.put("emails", emailSentiments);

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Get priority score based on sentiment
     */
    public int calculatePriorityScore(Map<String, Object> sentiment) {
        int score = 0;

        String urgency = (String) sentiment.getOrDefault("urgency", "low");
        score += switch (urgency) {
            case "critical" -> 40;
            case "high" -> 30;
            case "medium" -> 20;
            default -> 10;
        };

        String sentimentType = (String) sentiment.getOrDefault("sentiment", "neutral");
        score += switch (sentimentType) {
            case "angry" -> 30;
            case "negative" -> 20;
            case "neutral" -> 10;
            default -> 5;
        };

        return Math.min(score, 100);
    }

    private Map<String, Object> parseSentimentResponse(String response) {
        try {
            // Simple JSON parsing without external library
            Map<String, Object> result = new LinkedHashMap<>();
            String json = response.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
            }

            // Extract fields using basic string parsing
            result.put("sentiment", extractJsonValue(json, "sentiment"));
            result.put("confidence", Double.parseDouble(extractJsonValue(json, "confidence")));
            result.put("urgency", extractJsonValue(json, "urgency"));
            result.put("emotion", extractJsonValue(json, "emotion"));

            return result;
        } catch (Exception e) {
            log.warn("Failed to parse sentiment response, using defaults: {}", e.getMessage());
            return Map.of(
                    "sentiment", "neutral",
                    "confidence", 0.5,
                    "urgency", "low",
                    "emotion", "uncertain"
            );
        }
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";

        start += search.length();
        // Skip whitespace
        while (start < json.length() && json.charAt(start) == ' ') start++;

        if (start >= json.length()) return "";

        char quote = json.charAt(start);
        if (quote == '"') {
            start++;
            int end = json.indexOf('"', start);
            return end > start ? json.substring(start, end) : "";
        } else {
            // Number or boolean
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(start, end).trim();
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    /**
     * Sanitize text to prevent prompt injection attacks.
     * Removes or escapes characters that could manipulate LLM behavior.
     */
    private String sanitizeForPrompt(String text) {
        if (text == null) return "";
        String result = text;
        result = result.replace("``````", "");
        String[] lines = result.split("\n");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) continue;
            boolean isJsonLike = false;
            if (trimmed.startsWith("\"")) {
                isJsonLike = trimmed.contains("\":");
            } else if (trimmed.contains(":")) {
                String key = trimmed.substring(0, trimmed.indexOf(":")).strip();
                isJsonLike = !key.isEmpty() && key.chars().allMatch(Character::isLetterOrDigit);
            }
            if (!isJsonLike) {
                cleaned.append(line).append("\n");
            }
        }
        result = cleaned.toString();
        result = result.replaceAll("(?i)(ignore|disregard|forget|system|assistant|previous|instructions?)", "");
        return result.trim();
    }
}








