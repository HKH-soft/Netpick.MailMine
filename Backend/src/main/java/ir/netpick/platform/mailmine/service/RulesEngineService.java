package ir.netpick.platform.mailmine.service;

import ir.netpick.platform.mailmine.model.EmailMessage;
import ir.netpick.platform.mailmine.model.EmailRule;
import ir.netpick.platform.mailmine.model.EmailTag;
import ir.netpick.platform.mailmine.model.EmailTagAssignment;
import ir.netpick.platform.mailmine.repository.EmailRuleRepository;
import ir.netpick.platform.mailmine.repository.EmailTagRepository;
import ir.netpick.platform.mailmine.repository.EmailTagAssignmentRepository;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RulesEngineService {

    private final EmailRuleRepository emailRuleRepository;
    private final EmailTagRepository emailTagRepository;
    private final EmailTagAssignmentRepository emailTagAssignmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public void evaluateRules(EmailMessage emailMessage) {
        List<EmailRule> rules = emailRuleRepository.findByIsActiveTrueOrderByPriorityDesc();
        
        for (EmailRule rule : rules) {
            if (matchesCondition(emailMessage, rule)) {
                executeAction(emailMessage, rule);
            }
        }
    }

    private boolean matchesCondition(EmailMessage email, EmailRule rule) {
        return switch (rule.getConditionType()) {
            case SENDER_CONTAINS -> email.getSenderEmail() != null 
                    && email.getSenderEmail().toLowerCase().contains(rule.getConditionValue().toLowerCase());
            case SUBJECT_CONTAINS -> email.getSubject() != null 
                    && email.getSubject().toLowerCase().contains(rule.getConditionValue().toLowerCase());
            case BODY_CONTAINS -> email.getBodyText() != null 
                    && email.getBodyText().toLowerCase().contains(rule.getConditionValue().toLowerCase());
            case HAS_ATTACHMENT -> email.getHasAttachments();
            case TAG_MATCHES -> email.getEmailTags() != null 
                    && email.getEmailTags().stream()
                        .anyMatch(t -> t.getEmailTag().getName().equals(rule.getConditionValue()));
        };
    }

    private void executeAction(EmailMessage email, EmailRule rule) {
        switch (rule.getActionType()) {
            case ASSIGN_TO_USER -> {
                Optional<User> user = userRepository.findByEmail(rule.getActionValue());
                user.ifPresent(email::setAssignedTo);
            }
            case ADD_TAG -> {
                Optional<EmailTag> tag = emailTagRepository.findByName(rule.getActionValue());
                tag.ifPresent(t -> {
                    EmailTagAssignment assignment = new EmailTagAssignment();
                    assignment.setEmailMessage(email);
                    assignment.setEmailTag(t);
                    assignment.setIsAiGenerated(false);
                    emailTagAssignmentRepository.save(assignment);
                });
            }
            case MARK_AS_READ -> email.setIsRead(true);
            case SEND_NOTIFICATION -> {
                // Send notification via WebSocket
                log.info("Notification triggered for rule: {}", rule.getName());
            }
            case MOVE_TO_FOLDER -> {
                // Move email to specified folder
                String folderName = rule.getActionValue();
                if (folderName != null && !folderName.isEmpty()) {
                    email.setMailboxFolder(folderName);
                    log.info("Moved email {} to folder: {}", email.getMessageId(), folderName);
                }
            }
        }
    }
}








