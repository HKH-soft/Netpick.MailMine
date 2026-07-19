package ir.netpick.platform.gatekeeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record SecurityEventDTO(
        UUID id,
        String eventType,
        UUID userId,
        String userEmail,
        String ipAddress,
        String userAgent,
        String deviceFingerprint,
        String geoLocation,
        Map<String, Object> details,
        @JsonProperty("risk_score") int riskScore,
        boolean blocked,
        LocalDateTime createdAt
) {
}