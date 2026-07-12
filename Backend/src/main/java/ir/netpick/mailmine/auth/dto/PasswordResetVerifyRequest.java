package ir.netpick.mailmine.auth.dto;

public record PasswordResetVerifyRequest(
        String email,
        String code
) {
}