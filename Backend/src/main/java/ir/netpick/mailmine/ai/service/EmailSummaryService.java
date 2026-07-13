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
     * Generate summary for a single email
     */
    @Async
    public CompletableFuture<String> summarizeEmail(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        String prompt = String.format(SUMMARY_PROMPT,
                email.getSubject() != null ? email.getSubject() : "No subject",
                email.getSenderEmail(),
                email.getReceivedAt(),
                email.getBodyText() != null ? truncate(email.getBodyText(), 3000) : "No content");

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
                thread.get(0).getSubject(),
                threadContent);

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
                email.getSenderEmail(),
                email.getSubject(),
                email.getBodyText() != null ? truncate(email.getBodyText(), 2000) : "");

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
