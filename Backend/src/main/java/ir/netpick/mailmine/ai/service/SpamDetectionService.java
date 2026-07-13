package ir.netpick.mailmine.ai.service;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpamDetectionService {

    private final GeminiService geminiService;
    private final EmailMessageRepository emailMessageRepository;

    private static final String SPAM_DETECTION_PROMPT = """
        Analyze this email for spam, phishing, and scam indicators.
        
        From: %s
        Subject: %s
        Body: %s
        
        Check for:
        1. Phishing attempts (fake login pages, credential requests)
        2. CEO fraud (impersonation of executives)
        3. Fake invoices or payment requests
        4. Suspicious links or attachments
        5. Urgency tactics or threats
        6. Grammar/spelling red flags
        7. Known scam patterns
        
        Return a JSON response:
        {
            "is_spam": true/false,
            "is_phishing": true/false,
            "is_scam": true/false,
            "risk_level": "low" | "medium" | "high" | "critical",
            "confidence": 0.0-1.0,
            "threats": ["list of detected threats"],
            "recommendation": "one sentence recommendation"
        }
        """;

    @Async
    public CompletableFuture<Map<String, Object>> analyzeEmail(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        String prompt = String.format(SPAM_DETECTION_PROMPT,
                email.getSenderEmail(),
                email.getSubject(),
                email.getBodyText() != null ? truncate(email.getBodyText(), 3000) : "No content");

        String response = geminiService.generateText(prompt);
        Map<String, Object> result = parseSpamResponse(response);
        result.put("emailId", emailId.toString());
        result.put("sender", email.getSenderEmail());

        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Map<String, Object>> batchAnalyze(List<UUID> emailIds) {
        Map<String, Object> results = new LinkedHashMap<>();
        int spamCount = 0;
        int phishingCount = 0;

        for (UUID emailId : emailIds) {
            try {
                Map<String, Object> result = analyzeEmail(emailId).join();
                results.put(emailId.toString(), result);

                if (Boolean.TRUE.equals(result.get("is_spam"))) spamCount++;
                if (Boolean.TRUE.equals(result.get("is_phishing"))) phishingCount++;
            } catch (Exception e) {
                log.error("Failed to analyze email {}: {}", emailId, e.getMessage());
                results.put(emailId.toString(), Map.of("error", e.getMessage()));
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAnalyzed", emailIds.size());
        summary.put("spamDetected", spamCount);
        summary.put("phishingDetected", phishingCount);
        summary.put("results", results);

        return CompletableFuture.completedFuture(summary);
    }

    /**
     * Check if sender is suspicious based on domain reputation
     */
    public Map<String, Object> checkSenderReputation(String email) {
        Map<String, Object> reputation = new LinkedHashMap<>();
        reputation.put("email", email);

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        // Known suspicious patterns
        List<String> suspiciousPatterns = List.of(
                "tempmail", "throwaway", "guerrilla", "mailinator",
                "yopmail", "guerrillamail", "sharklasers"
        );

        boolean isDisposable = suspiciousPatterns.stream()
                .anyMatch(domain::contains);
        reputation.put("isDisposable", isDisposable);
        reputation.put("domain", domain);

        return reputation;
    }

    private Map<String, Object> parseSpamResponse(String response) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            String json = response.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
            }

            result.put("is_spam", extractJsonBoolean(json, "is_spam"));
            result.put("is_phishing", extractJsonBoolean(json, "is_phishing"));
            result.put("is_scam", extractJsonBoolean(json, "is_scam"));
            result.put("risk_level", extractJsonValue(json, "risk_level"));
            result.put("confidence", Double.parseDouble(extractJsonValue(json, "confidence")));
            result.put("recommendation", extractJsonValue(json, "recommendation"));

            return result;
        } catch (Exception e) {
            log.warn("Failed to parse spam response, using defaults: {}", e.getMessage());
            return Map.of(
                    "is_spam", false,
                    "is_phishing", false,
                    "is_scam", false,
                    "risk_level", "unknown",
                    "confidence", 0.5
            );
        }
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";

        start += search.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;

        if (start >= json.length()) return "";

        char quote = json.charAt(start);
        if (quote == '"') {
            start++;
            int end = json.indexOf('"', start);
            return end > start ? json.substring(start, end) : "";
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(start, end).trim();
        }
    }

    private boolean extractJsonBoolean(String json, String key) {
        String value = extractJsonValue(json, key);
        return "true".equalsIgnoreCase(value);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
