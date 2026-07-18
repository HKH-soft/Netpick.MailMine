package ir.netpick.platform.gatekeeper.dto;

public record MfaSetupResponse(
        String secret,
        String qrCodeUrl,
        String backupCodes
) {
}
