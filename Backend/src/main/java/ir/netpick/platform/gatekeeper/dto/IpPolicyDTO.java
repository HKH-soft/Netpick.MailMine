package ir.netpick.platform.gatekeeper.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record IpPolicyDTO(
        UUID id,
        String policyName,
        String policyType,
        String ipAddress,
        String ipRangeStart,
        String ipRangeEnd,
        String cidrNotation,
        String description,
        boolean active,
        Instant expiresAt,
        String createdByEmail,
        LocalDateTime createdAt
) {
}
