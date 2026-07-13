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
public class EmailSummaryService {

    private final GeminiService geminiService;
    private final EmailMessageRepository emailMessageRepository;

    private static final String SUMMARY_PROMPT = """
        Summarize this email conversation concisely.
        
        Subject: %s
        From: %s
        Date: %s
        Body:
        %s
        
        Return a structured summary with:
        1. Key points (bullet list)
        2. Action items if any
        3. Sentiment (positive/neutral/negative)
        Keep it under 200 words.
        """;

    /**
     * Sanitize text to prevent prompt injection attacks.
     */
    private String sanitizeForPrompt(String text) {
        if (text == null) return "";
        String result = text;
        result = result.replace("````", "");
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

    /**
     * Generate summary for a single email
     */
    @Async
    public CompletableFuture<String> summarizeEmail(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        String prompt = String.format(SUMMARY_PROMPT,
                sanitizeForPrompt(email.getSubject() != null ? email.getSubject() : "No subject"),
                sanitizeForPrompt(email.getSenderEmail()),
                email.getReceivedAt(),
                sanitizeForPrompt(email.getBodyText() != null ? truncate(email.getBodyText(), 3000) : "No content"));

        String summary = geminiService.generateText(prompt);
        return CompletableFuture.completedFuture(summary);
    }

    /**
     * Generate summary for an email thread
     */
    @Async
    public CompletableFuture<String> summarizeThread(String threadId) {
        List<EmailMessage> thread = emailMessageRepository
                .findByThreadIdOrderByReceivedAtAsc(threadId);

        if (thread.isEmpty()) {
            return CompletableFuture.completedFuture("No emails found in thread");
        }

        StringBuilder threadContent = new StringBuilder();
        for (EmailMessage email : thread) {
            threadContent.append(String.format("From: %s | Date: %s\nSubject: %s\n%s\n---\n",
                    email.getSenderEmail(),
                    email.getReceivedAt(),
                    email.getSubject(),
                    email.getBodyText() != null ? truncate(email.getBodyText(), 1000) : ""));
        }

        String prompt = String.format("""
            Summarize this email thread (%d emails).
            
            Subject: %s
            
            Thread content:
            %s
            
            Return a structured summary with:
            1. Overview (2-3 sentences)
            2. Key discussion points
            3. Decisions made
            4. Pending action items
            5. Current status
            """,
                thread.size(),
                sanitizeForPrompt(thread.get(0).getSubject()),
                sanitizeForPrompt(threadContent.toString()));

        String summary = geminiService.generateText(prompt);
        return CompletableFuture.completedFuture(summary);
    }

    /**
     * Generate customer status card
     */
    @Async
    public CompletableFuture<Map<String, Object>> generateCustomerStatus(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        List<EmailMessage> thread = email.getThreadId() != null
                ? emailMessageRepository.findByThreadIdOrderByReceivedAtAsc(email.getThreadId())
                : List.of(email);

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("customer", email.getSenderEmail());
        status.put("subject", email.getSubject());
        status.put("emailCount", thread.size());

        long hoursSinceLastReply = email.getLastReplyAt() != null
                ? java.time.temporal.ChronoUnit.HOURS.between(email.getLastReplyAt(), java.time.LocalDateTime.now())
                : java.time.temporal.ChronoUnit.HOURS.between(email.getReceivedAt(), java.time.LocalDateTime.now());
        status.put("hoursSinceLastReply", hoursSinceLastReply);

        boolean awaitingReply = !email.getIsAnswered() && hoursSinceLastReply > 24;
        status.put("awaitingReply", awaitingReply);

        String prompt = String.format("""
            Based on the latest email from %s about "%s":
            
            %s
            
            Respond with exactly one of these statuses:
            - WAITING_FOR_QUOTE
            - FOLLOW_UP_NEEDED
            - ACTION_REQUIRED
            - RESOLVED
            - PENDING_CUSTOMER_RESPONSE
            
            Then add a one-sentence explanation.
            """,
                sanitizeForPrompt(email.getSenderEmail()),
                sanitizeForPrompt(email.getSubject()),
                sanitizeForPrompt(email.getBodyText() != null ? truncate(email.getBodyText(), 2000) : ""));

        String aiStatus = geminiService.generateText(prompt);
        status.put("aiStatus", aiStatus);
        status.put("thread", thread.stream().map(e -> Map.of(
                "id", e.getId().toString(),
                "subject", e.getSubject(),
                "from", e.getSenderEmail(),
                "date", e.getReceivedAt().toString(),
                "isRead", e.getIsAnswered()
        )).toList());

        return CompletableFuture.completedFuture(status);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}








