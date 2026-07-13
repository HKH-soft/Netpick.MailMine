package ir.netpick.platform.mailmine.controller;

import ir.netpick.platform.mailmine.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mailmine/attachments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/email/{emailId}")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable UUID emailId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(attachmentService.saveAttachment(emailId, file));
    }

    @GetMapping("/email/{emailId}")
    public ResponseEntity<List<Map<String, Object>>> getAttachments(@PathVariable UUID emailId) {
        return ResponseEntity.ok(attachmentService.getAttachments(emailId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchAttachments(
            @RequestParam String query) {
        return ResponseEntity.ok(attachmentService.searchAttachments(query));
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable String filename) throws IOException {
        attachmentService.deleteAttachment(filename);
        return ResponseEntity.noContent().build();
    }
}









