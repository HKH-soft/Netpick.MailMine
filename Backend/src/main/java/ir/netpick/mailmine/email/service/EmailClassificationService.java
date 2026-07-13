package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.model.EmailTag;
import ir.netpick.mailmine.email.model.EmailTagAssignment;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import ir.netpick.mailmine.email.repository.EmailTagAssignmentRepository;
import ir.netpick.mailmine.email.repository.EmailTagRepository;
import ir.netpick.mailmine.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailClassificationService {

    private final GeminiService geminiService;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailTagRepository emailTagRepository;
    private final EmailTagAssignmentRepository emailTagAssignmentRepository;

    private static final String CLASSIFICATION_PROMPT = """
        Classify this email into ONE of these categories:
        - SALES_LEAD: Someone interested in buying products/services
        - CUSTOMER: Existing customer inquiry or support
        - SUPPLIER: Vendor/supplier communication
        - INVOICE: Billing, invoice, payment related
        - SUPPORT: Technical support request
        - HR: Human resources, recruitment, internal staff
        - LEGAL: Legal matters, contracts, compliance
        - SPAM: Unsolicited, promotional, irrelevant
        - NEWSLETTER: Newsletter, update, announcement
        
        Email subject: %s
        Email body: %s
        
        Return only the category name, no explanation.
        """;

    @Transactional
    public List<EmailTag> classifyEmail(EmailMessage emailMessage) {
        try {
            String prompt = String.format(CLASSIFICATION_PROMPT,
                    emailMessage.getSubject() != null ? emailMessage.getSubject() : "",
                    emailMessage.getBodyText() != null ? emailMessage.getBodyText() : "");

            String category = geminiService.generateText(prompt).trim().toUpperCase();
            
            EmailTag.TagCategory tagCategory = mapToTagCategory(category);
            if (tagCategory == null) {
                log.warn("Unknown classification category: {}", category);
                return Collections.emptyList();
            }

            List<EmailTag> tags = emailTagRepository.findByCategory(tagCategory);
            EmailTag emailTag;
            if (tags.isEmpty()) {
                emailTag = createDefaultTag(tagCategory);
            } else {
                emailTag = tags.get(0);
            }

            EmailTagAssignment assignment = new EmailTagAssignment();
            assignment.setEmailMessage(emailMessage);
            assignment.setEmailTag(emailTag);
            assignment.setIsAiGenerated(true);
            assignment.setConfidenceScore(0.85);
            
            emailTagAssignmentRepository.save(assignment);
            
            return Collections.singletonList(emailTag);
        } catch (Exception e) {
            log.error("Failed to classify email {}: {}", emailMessage.getId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private EmailTag.TagCategory mapToTagCategory(String category) {
        try {
            return EmailTag.TagCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            return EmailTag.TagCategory.OTHER;
        }
    }

    private EmailTag createDefaultTag(EmailTag.TagCategory category) {
        EmailTag tag = new EmailTag();
        tag.setName(category.name());
        tag.setCategory(category);
        tag.setDescription("Auto-generated tag for " + category.name().toLowerCase());
        return emailTagRepository.save(tag);
    }

    @Transactional
    public void classifyUnprocessedEmails() {
        List<EmailMessage> unprocessed = emailMessageRepository.findAll()
                .stream()
                .filter(e -> e.getEmailTags() == null || e.getEmailTags().isEmpty())
                .limit(100)
                .collect(Collectors.toList());

        for (EmailMessage email : unprocessed) {
            classifyEmail(email);
        }
    }
}