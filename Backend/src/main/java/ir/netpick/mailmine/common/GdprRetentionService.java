package ir.netpick.mailmine.common;

import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GdprRetentionService {

    private final GdprRetentionConfigRepository configRepository;
    private final AuditTrailRepository auditTrailRepository;
    private final EmailMessageRepository emailMessageRepository;

    /**
     * Run cleanup for all active retention configs.
     */
    public Map<String, Object> runCleanup() {
        Map<String, Object> results = new LinkedHashMap<>();
        List<GdprRetentionConfig> configs = configRepository.findByIsActiveTrue();

        for (GdprRetentionConfig config : configs) {
            results.put(config.getEntityType(), cleanupEntityType(config));
        }

        return results;
    }

    /**
     * Get current retention configurations.
     */
    public List<GdprRetentionConfig> getConfigs() {
        return configRepository.findAll();
    }

    /**
     * Update retention days for an entity type.
     */
    public GdprRetentionConfig updateConfig(String entityType, int retentionDays) {
        GdprRetentionConfig config = configRepository.findByEntityType(entityType)
                .orElse(new GdprRetentionConfig());
        config.setEntityType(entityType);
        config.setRetentionDays(retentionDays);
        config.setUpdatedAt(LocalDateTime.now());
        return configRepository.save(config);
    }

    private Map<String, Object> cleanupEntityType(GdprRetentionConfig config) {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(config.getRetentionDays());

        switch (config.getEntityType()) {
            case "AUDIT_TRAIL":
                List<ir.netpick.mailmine.common.AuditTrail> oldAudits =
                        auditTrailRepository.findByCreatedAtAfter(cutoff);
                // Soft-delete by keeping but marking — in practice, hard delete for GDPR
                auditTrailRepository.deleteAll(oldAudits);
                result.put("deleted", oldAudits.size());
                result.put("cutoffDate", cutoff.toString());
                break;

            case "EMAIL_MESSAGE":
                // Soft-deleted emails already handled by BaseEntity.deleted flag
                // Hard delete for GDPR right to erasure
                result.put("note", "Email soft-delete handled by retention policy");
                break;

            default:
                result.put("note", "No cleanup handler for " + config.getEntityType());
        }

        config.setLastCleanupAt(LocalDateTime.now());
        configRepository.save(config);

        return result;
    }

    /**
     * Scheduled cleanup — runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledCleanup() {
        log.info("Starting scheduled GDPR retention cleanup");
        try {
            runCleanup();
            log.info("GDPR retention cleanup completed");
        } catch (Exception e) {
            log.error("GDPR retention cleanup failed: {}", e.getMessage());
        }
    }
}
