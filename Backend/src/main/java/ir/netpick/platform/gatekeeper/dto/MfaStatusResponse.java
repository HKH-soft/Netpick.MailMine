package ir.netpick.platform.gatekeeper.dto;

import java.time.LocalDateTime;

public record MfaStatusResponse(
        boolean mfaEnabled,
        boolean totpVerified,
        long backupCodesRemaining,
        LocalDateTime lastUsedAt
) {
}
