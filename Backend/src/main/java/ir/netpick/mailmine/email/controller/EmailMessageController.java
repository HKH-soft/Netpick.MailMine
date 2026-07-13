package ir.netpick.mailmine.email.controller;

import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import ir.netpick.mailmine.email.service.EmailClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class EmailMessageController {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailClassificationService emailClassificationService;

    @GetMapping
    public ResponseEntity<Page<EmailMessage>> listEmails(Pageable pageable) {
        return ResponseEntity.ok(emailMessageRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailMessage> getEmail(@PathVariable UUID id) {
        return emailMessageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/classify")
    public ResponseEntity<Void> classifyEmail(@PathVariable UUID id) {
        return emailMessageRepository.findById(id)
                .map(email -> {
                    emailClassificationService.classifyEmail(email);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tag/{tagId}")
    public ResponseEntity<Page<EmailMessage>> getEmailsByTag(
            @PathVariable UUID tagId, 
            Pageable pageable) {
        return ResponseEntity.ok(emailMessageRepository.findByTagId(tagId, pageable));
    }
}