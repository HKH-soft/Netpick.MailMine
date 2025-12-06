package ir.netpick.mailmine.auth.dto;

public record AuthConfigResponse(
        int resendCooldownSeconds,
        int resendMaxPerHour) {
}
