package ir.netpick.platform.gatekeeper.dto;

public record PasswordResetConfirmRequest(
        String email,
        String code,
        String password
) {
}








