package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.model.EmailTag;
import ir.netpick.mailmine.email.model.EmailTagAssignment;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import ir.netpick.mailmine.email.repository.EmailTagAssignmentRepository;
import ir.netpick.mailmine.email.repository.EmailTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTagService {

    private final EmailTagRepository emailTagRepository;
    private final EmailTagAssignmentRepository emailTagAssignmentRepository;
    private final EmailMessageRepository emailMessageRepository;

    public Page<EmailTag> listAll(Pageable pageable) {
        return emailTagRepository.findAll(pageable);
    }

    public List<EmailTag> listByCategory(EmailTag.TagCategory category) {
        return emailTagRepository.findByCategory(category);
    }

    public EmailTag getById(UUID id) {
        return emailTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + id));
    }

    public EmailTag getByName(String name) {
        return emailTagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + name));
    }

    public EmailTag create(EmailTag tag) {
        if (emailTagRepository.findByName(tag.getName()).isPresent()) {
            throw new IllegalArgumentException("Tag with name " + tag.getName() + " already exists");
        }
        return emailTagRepository.save(tag);
    }

    public EmailTag update(UUID id, EmailTag updates) {
        EmailTag existing = getById(id);
        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setCategory(updates.getCategory());
        existing.setColorHex(updates.getColorHex());
        return emailTagRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        EmailTag tag = getById(id);
        tag.setDeleted(true);
        emailTagRepository.save(tag);
    }

    @Transactional
    public EmailTagAssignment assignTag(UUID emailId, UUID tagId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));
        EmailTag tag = getById(tagId);

        EmailTagAssignment assignment = new EmailTagAssignment();
        assignment.setEmailMessage(email);
        assignment.setEmailTag(tag);
        assignment.setIsAiGenerated(false);
        return emailTagAssignmentRepository.save(assignment);
    }

    @Transactional
    public void removeTagAssignment(UUID emailId, UUID tagId) {
        emailTagAssignmentRepository.deleteByEmailMessageId(emailId);
    }

    public List<EmailTagAssignment> getEmailTags(UUID emailId) {
        return emailTagAssignmentRepository.findByEmailMessageId(emailId);
    }

    public Page<EmailMessage> getEmailsByTag(UUID tagId, Pageable pageable) {
        return emailMessageRepository.findByTagId(tagId, pageable);
    }
}
