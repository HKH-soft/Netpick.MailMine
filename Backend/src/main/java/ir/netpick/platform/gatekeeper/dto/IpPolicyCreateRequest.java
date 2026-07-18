package ir.netpick.platform.gatekeeper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record IpPolicyCreateRequest(
        @NotBlank(message = "Policy name is required")
        String policyName,

        @NotNull(message = "Policy type is required")
        String policyType,

        String ipAddress,
        String ipRangeStart,
        String ipRangeEnd,
        String cidrNotation,
        String description,
        Instant expiresAt
) {
}
