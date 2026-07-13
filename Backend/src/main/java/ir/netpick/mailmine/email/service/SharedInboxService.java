package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.model.SharedInbox;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import ir.netpick.mailmine.email.repository.SharedInboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedInboxService {

    private final SharedInboxRepository sharedInboxRepository;
    private final UserRepository userRepository;
    private final EmailMessageRepository emailMessageRepository;

    public Page<SharedInbox> listAll(Pageable pageable) {
        return sharedInboxRepository.findAll(pageable);
    }

    public SharedInbox getById(UUID id) {
        return sharedInboxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SharedInbox not found: " + id));
    }

    public SharedInbox create(SharedInbox inbox) {
        if (sharedInboxRepository.findByEmailAddress(inbox.getEmailAddress()).isPresent()) {
            throw new IllegalArgumentException("SharedInbox with email " + inbox.getEmailAddress() + " already exists");
        }
        return sharedInboxRepository.save(inbox);
    }

    public SharedInbox update(UUID id, SharedInbox updates) {
        SharedInbox existing = getById(id);
        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setEmailAddress(updates.getEmailAddress());
        existing.setIsActive(updates.getIsActive());
        return sharedInboxRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        SharedInbox inbox = getById(id);
        inbox.setDeleted(true);
        sharedInboxRepository.save(inbox);
    }

    @Transactional
    public SharedInbox addMember(UUID inboxId, UUID userId) {
        SharedInbox inbox = getById(inboxId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        inbox.getMembers().add(user);
        return sharedInboxRepository.save(inbox);
    }

    @Transactional
    public SharedInbox removeMember(UUID inboxId, UUID userId) {
        SharedInbox inbox = getById(inboxId);
        inbox.getMembers().removeIf(u -> u.getId().equals(userId));
        return sharedInboxRepository.save(inbox);
    }

    public Set<User> getMembers(UUID inboxId) {
        return getById(inboxId).getMembers();
    }

    @Transactional
    public EmailMessage assignEmail(UUID inboxId, UUID emailId, UUID userId) {
        getById(inboxId); // validates inbox exists
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        email.setAssignedTo(assignee);
        email.setAssignedAt(LocalDateTime.now());
        email.setStatus(EmailMessage.EmailStatus.ASSIGNED);
        return emailMessageRepository.save(email);
    }

    public Page<EmailMessage> getInboxEmails(UUID inboxId, Pageable pageable) {
        return emailMessageRepository.findByStatus(EmailMessage.EmailStatus.INBOX, pageable);
    }

    public Page<EmailMessage> getAssignedEmails(UUID userId, Pageable pageable) {
        return emailMessageRepository.findByAssignedToId(userId, pageable);
    }
}
