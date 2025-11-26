package ir.netpick.mailmine.auth.dto;

public record VerificationRequest(
        String email,
        String code
) {
}
