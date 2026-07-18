package ir.netpick.platform.gatekeeper.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MfaStatusResponse(
        boolean mfaEnabled,
        boolean totpVerified,
        long backupCodesRemaining,
        LocalDateTime lastUsedAt
) {
}
