package ir.netpick.platform.ai.controller;

import ir.netpick.platform.ai.service.SpamDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/core/spam-detection")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class SpamDetectionController {

    private final SpamDetectionService spamDetectionService;

    @GetMapping("/email/{emailId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> analyzeEmail(
            @PathVariable UUID emailId) {
        return spamDetectionService.analyzeEmail(emailId)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/batch")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> batchAnalyze(
            @RequestBody List<UUID> emailIds) {
        return spamDetectionService.batchAnalyze(emailIds)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/sender-reputation")
    public ResponseEntity<Map<String, Object>> checkSenderReputation(
            @RequestParam String email) {
        return ResponseEntity.ok(spamDetectionService.checkSenderReputation(email));
    }
}








