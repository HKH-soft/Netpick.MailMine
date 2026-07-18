package ir.netpick.platform.gatekeeper.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeviceSessionDTO(
        UUID id,
        String deviceInfo,
        String deviceFingerprint,
        String ipAddress,
        String geoLocation,
        LocalDateTime lastActiveAt,
        Instant expiresAt,
        boolean revoked,
        boolean currentSession,
        LocalDateTime createdAt
) {
}
