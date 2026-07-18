package ir.netpick.platform.gatekeeper.dto;

import jakarta.validation.constraints.NotBlank;

public record MfaDisableRequest(
        @NotBlank(message = "Password is required for MFA disable")
        String password,

        String totpCode
) {
}
