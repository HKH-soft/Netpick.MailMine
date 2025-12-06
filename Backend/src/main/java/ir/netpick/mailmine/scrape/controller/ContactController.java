package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.base.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/scrape/contacts")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ContactController {
    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<?> allContacts(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(contactService.allContacts(page));
    }

    @GetMapping("/deleted")
    public ResponseEntity<?> deletedContacts(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(contactService.deletedContacts(page));
    }

    @GetMapping("/all")
    public ResponseEntity<?> allContactsIncludingDeleted(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(contactService.allContactsIncludingDeleted(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getContact(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .body(contactService.getContact(id));
    }

    @GetMapping("/deleted/{id}")
    public ResponseEntity<?> getDeletedContact(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .body(contactService.getContactIncludingDeleted(id));
    }

    @PutMapping("{id}/restore")
    public ResponseEntity<?> restoreContact(@PathVariable UUID id) {
        contactService.restoreContact(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> softDeleteContact(@PathVariable UUID id) {
        contactService.softDeleteContact(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}/full")
    public ResponseEntity<?> fullDeleteContact(@PathVariable UUID id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(contactService.getStats());
    }
}