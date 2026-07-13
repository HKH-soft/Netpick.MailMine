package ir.netpick.platform.core;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/core/gdpr")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class GdprController {

    private final GdprRetentionService retentionService;
    private final AuditTrailRepository auditTrailRepository;

    @GetMapping("/configs")
    public ResponseEntity<List<GdprRetentionConfig>> getConfigs() {
        return ResponseEntity.ok(retentionService.getConfigs());
    }

    @PutMapping("/configs/{entityType}")
    public ResponseEntity<GdprRetentionConfig> updateConfig(
            @PathVariable String entityType,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(retentionService.updateConfig(entityType, body.get("retentionDays")));
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> runCleanup() {
        return ResponseEntity.ok(retentionService.runCleanup());
    }

    @GetMapping("/audit-trail/{entityType}/{entityId}")
    public ResponseEntity<List<AuditTrail>> getAuditTrail(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        return ResponseEntity.ok(
                auditTrailRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        entityType, java.util.UUID.fromString(entityId)));
    }

    @GetMapping("/audit-trail/recent")
    public ResponseEntity<List<AuditTrail>> getRecentAuditTrail(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(
                auditTrailRepository.findByCreatedAtAfter(
                        java.time.LocalDateTime.now().minusDays(7)));
    }
}








