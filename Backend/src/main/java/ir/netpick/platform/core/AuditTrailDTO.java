package ir.netpick.platform.core;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditTrailDTO(
        UUID id,
        String entityType,
        UUID entityId,
        String action,
        UUID performedById,
        String performedByEmail,
        String oldValues,
        String newValues,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {}