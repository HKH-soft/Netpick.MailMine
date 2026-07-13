package ir.netpick.mailmine.email.controller;

import ir.netpick.mailmine.email.model.SharedInbox;
import ir.netpick.mailmine.email.service.SharedInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shared-inboxes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class SharedInboxController {

    private final SharedInboxService sharedInboxService;

    @GetMapping
    public ResponseEntity<Page<SharedInbox>> listInboxes(Pageable pageable) {
        return ResponseEntity.ok(sharedInboxService.listAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SharedInbox> getInbox(@PathVariable UUID id) {
        return ResponseEntity.ok(sharedInboxService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SharedInbox> createInbox(@RequestBody SharedInbox inbox) {
        return ResponseEntity.ok(sharedInboxService.create(inbox));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SharedInbox> updateInbox(
            @PathVariable UUID id,
            @RequestBody SharedInbox inbox) {
        return ResponseEntity.ok(sharedInboxService.update(id, inbox));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInbox(@PathVariable UUID id) {
        sharedInboxService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{inboxId}/members/{userId}")
    public ResponseEntity<SharedInbox> addMember(
            @PathVariable UUID inboxId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(sharedInboxService.addMember(inboxId, userId));
    }

    @DeleteMapping("/{inboxId}/members/{userId}")
    public ResponseEntity<SharedInbox> removeMember(
            @PathVariable UUID inboxId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(sharedInboxService.removeMember(inboxId, userId));
    }

    @PostMapping("/{inboxId}/assign/{emailId}/{userId}")
    public ResponseEntity<?> assignEmail(
            @PathVariable UUID inboxId,
            @PathVariable UUID emailId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(sharedInboxService.assignEmail(inboxId, emailId, userId));
    }
}
