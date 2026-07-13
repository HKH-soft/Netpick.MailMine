package ir.netpick.platform.gatekeeper.dto;

public record AuthConfigResponse(
        int resendCooldownSeconds,
        int resendMaxPerHour) {
}









