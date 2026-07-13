package ir.netpick.platform.mailmine.controller;

import ir.netpick.platform.mailmine.dto.EmailRequest;
import ir.netpick.platform.mailmine.service.EmailQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mailmine/email-queue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class EmailQueueController {

    private final EmailQueueService emailQueueService;

    @PostMapping
    public ResponseEntity<?> queueEmail(
            @RequestBody EmailRequest request,
            @RequestAttribute("userId") UUID userId) {
        UUID itemId = emailQueueService.queueEmail(request, userId);
        return ResponseEntity.ok().body(java.util.Map.of("id", itemId, "status", "QUEUED"));
    }

    @GetMapping("/status/{itemId}")
    public ResponseEntity<?> getQueueItemStatus(@PathVariable UUID itemId) {
        return ResponseEntity.ok().body(java.util.Map.of("id", itemId));
    }
}









