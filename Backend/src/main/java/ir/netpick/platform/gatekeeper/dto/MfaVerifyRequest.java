package ir.netpick.platform.gatekeeper.dto;

import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequest(
        @NotBlank(message = "TOTP code is required")
        String totpCode
) {
}
