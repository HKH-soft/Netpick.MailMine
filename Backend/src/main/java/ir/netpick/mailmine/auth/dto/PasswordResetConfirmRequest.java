package ir.netpick.mailmine.auth.dto;

public record PasswordResetConfirmRequest(
        String email,
        String code,
        String password
) {
}