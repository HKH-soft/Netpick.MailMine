package ir.netpick.platform.gatekeeper.dto;

import jakarta.validation.constraints.NotBlank;

public record MfaEnableRequest(
        @NotBlank(message = "TOTP code is required")
        String totpCode,

        @NotBlank(message = "Setup secret is required")
        String setupSecret
) {
}
