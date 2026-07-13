package ir.netpick.platform.gatekeeper.dto;

public record PasswordResetVerifyRequest(
        String email,
        String code
) {
}








