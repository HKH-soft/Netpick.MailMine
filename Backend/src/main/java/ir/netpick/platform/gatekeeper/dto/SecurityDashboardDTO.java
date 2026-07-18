package ir.netpick.platform.gatekeeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SecurityDashboardDTO(
        @JsonProperty("total_events_24h") long totalEvents24h,
        @JsonProperty("failed_logins_24h") long failedLogins24h,
        @JsonProperty("blocked_requests_24h") long blockedRequests24h,
        @JsonProperty("high_risk_events_24h") long highRiskEvents24h,
        @JsonProperty("active_sessions") long activeSessions,
        @JsonProperty("active_users_24h") long activeUsers24h,
        @JsonProperty("top_attack_ips") List<TopIpEntry> topAttackIps,
        @JsonProperty("recent_events") List<SecurityEventDTO> recentEvents
) {
    public record TopIpEntry(
            String ip,
            long count
    ) {
}
}
