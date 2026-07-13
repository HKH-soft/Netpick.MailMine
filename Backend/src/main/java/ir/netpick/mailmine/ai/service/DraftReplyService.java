package ir.netpick.mailmine.ai.service;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.model.EmailTemplate;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import ir.netpick.mailmine.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftReplyService {

    private final GeminiService geminiService;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailTemplateRepository emailTemplateRepository;

    private static final String DRAFT_PROMPT = """
        You are a professional email assistant. Generate a reply to this email.
        
        From: %s
        Subject: %s
        Original message:
        %s
        
        Context (optional):
        %s
        
        Requirements:
        - Professional and polite tone
        - Address the customer by name if available
        - Reference specific points from their email
        - Keep it concise but thorough
        - Include a clear call to action
        
        Generate only the reply body, no subject line.
        """;

    private static final String DRAFT_WITH_TEMPLATE_PROMPT = """
        You are a professional email assistant. Generate a reply using this template as a base.
        
        Template:
        %s
        
        Original email:
        From: %s
        Subject: %s
        Body: %s
        
        Personalize the template with the customer's information.
        Fill in any {{variables}} with appropriate content.
        Generate only the reply body.
        """;

    /**
     * Generate a draft reply to an email
     */
    @Async
    public CompletableFuture<String> generateDraft(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        String prompt = String.format(DRAFT_PROMPT,
                email.getSenderEmail(),
                email.getSubject(),
                email.getBodyText() != null ? truncate(email.getBodyText(), 3000) : "No content",
                "");

        String draft = geminiService.generateText(prompt);
        return CompletableFuture.completedFuture(draft);
    }

    /**
     * Generate a draft reply using a template
     */
    @Async
    public CompletableFuture<String> generateDraftWithTemplate(UUID emailId, UUID templateId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));

        String prompt = String.format(DRAFT_WITH_TEMPLATE_PROMPT,
                template.getBodyTemplate(),
                email.getSenderEmail(),
                email.getSubject(),
                email.getBodyText() != null ? truncate(email.getBodyText(), 3000) : "No content");

        String draft = geminiService.generateText(prompt);
        return CompletableFuture.completedFuture(draft);
    }

    /**
     * Generate subject line suggestions
     */
    @Async
    @SuppressWarnings("nullness")
    public CompletableFuture<List<String>> generateSubjectSuggestions(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        String prompt = String.format("""
            Generate 5 alternative subject lines for a reply to this email:
            
            Original subject: %s
            From: %s
            Body: %s
            
            Return ONLY the 5 subject lines, one per line, no numbering or bullet points.
            """,
                email.getSubject(),
                email.getSenderEmail(),
                email.getBodyText() != null ? truncate(email.getBodyText(), 1000) : "");

        String response = geminiService.generateText(prompt);
        List<String> subjects = Arrays.stream(response.split("\n"))
                .map(s -> s != null ? s.trim() : null)
                .filter(s -> s != null && !s.isEmpty())
                .limit(5)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(subjects);
    }

    /**
     * Improve an existing draft
     */
    @Async
    public CompletableFuture<String> improveDraft(String draft, String instructions) {
        String prompt = String.format("""
            Improve this email draft based on the instructions:
            
            Original draft:
            %s
            
            Instructions: %s
            
            Return the improved draft.
            """,
                draft, instructions);

        String improved = geminiService.generateText(prompt);
        return CompletableFuture.completedFuture(improved);
    }

    /**
     * Generate reply with company context
     */
    @Async
    public CompletableFuture<String> generateContextualReply(UUID emailId, String companyContext) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        String prompt = String.format(DRAFT_PROMPT,
                email.getSenderEmail(),
                email.getSubject(),
                email.getBodyText() != null ? truncate(email.getBodyText(), 3000) : "No content",
                "Company information: " + companyContext);

        String draft = geminiService.generateText(prompt);
        return CompletableFuture.completedFuture(draft);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
