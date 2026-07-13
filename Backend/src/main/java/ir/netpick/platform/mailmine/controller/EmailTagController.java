package ir.netpick.platform.mailmine.controller;

import ir.netpick.platform.mailmine.model.EmailTag;
import ir.netpick.platform.mailmine.service.EmailTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mailmine/email-tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class EmailTagController {

    private final EmailTagService emailTagService;

    @GetMapping
    public ResponseEntity<Page<EmailTag>> listTags(Pageable pageable) {
        return ResponseEntity.ok(emailTagService.listAll(pageable));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<EmailTag>> listByCategory(
            @PathVariable EmailTag.TagCategory category) {
        return ResponseEntity.ok(emailTagService.listByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTag> getTag(@PathVariable UUID id) {
        return ResponseEntity.ok(emailTagService.getById(id));
    }

    @PostMapping
    public ResponseEntity<EmailTag> createTag(@RequestBody EmailTag tag) {
        return ResponseEntity.ok(emailTagService.create(tag));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTag> updateTag(
            @PathVariable UUID id,
            @RequestBody EmailTag tag) {
        return ResponseEntity.ok(emailTagService.update(id, tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        emailTagService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign/{emailId}/{tagId}")
    public ResponseEntity<?> assignTag(
            @PathVariable UUID emailId,
            @PathVariable UUID tagId) {
        return ResponseEntity.ok(emailTagService.assignTag(emailId, tagId));
    }

    @GetMapping("/email/{emailId}")
    public ResponseEntity<?> getEmailTags(@PathVariable UUID emailId) {
        return ResponseEntity.ok(emailTagService.getEmailTags(emailId));
    }
}









