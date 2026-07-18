package ir.netpick.platform.gatekeeper.dto;

import jakarta.validation.constraints.NotBlank;

public record MfaBackupCodeRequest(
        @NotBlank(message = "Backup code is required")
        String backupCode
) {
}
