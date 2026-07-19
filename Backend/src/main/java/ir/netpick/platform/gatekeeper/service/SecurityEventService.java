package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.dto.SecurityDashboardDTO;
import ir.netpick.platform.gatekeeper.dto.SecurityEventDTO;
import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import ir.netpick.platform.gatekeeper.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    @Async
    public void logEvent(SecurityEvent.EventType eventType, UUID userId, String userEmail,
                         String ipAddress, String userAgent, String deviceFingerprint,
                         Map<String, Object> details, int riskScore, boolean blocked) {
        try {
            SecurityEvent event = new SecurityEvent();
            event.setEventType(eventType.name());
            event.setUserId(userId);
            event.setUserEmail(userEmail);
            event.setIpAddress(ipAddress);
            event.setUserAgent(userAgent);
            event.setDeviceFingerprint(deviceFingerprint);
            event.setDetails(details);
            event.setRiskScore(riskScore);
            event.setBlocked(blocked);
            securityEventRepository.save(event);

            if (riskScore >= 50) {
                log.warn("HIGH RISK event: {} user={} ip={} risk={}", eventType, userEmail, ipAddress, riskScore);
            }
        } catch (Exception e) {
            log.error("Failed to log security event: {}", e.getMessage());
        }
    }

    public void logEventSync(SecurityEvent.EventType eventType, UUID userId, String userEmail,
                             String ipAddress, String userAgent, String deviceFingerprint,
                             Map<String, Object> details, int riskScore, boolean blocked) {
        logEvent(eventType, userId, userEmail, ipAddress, userAgent, deviceFingerprint, details, riskScore, blocked);
    }

    public Page<SecurityEvent> getEventsByUser(UUID userId, int page, int size) {
        return securityEventRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public Page<SecurityEvent> getRecentEvents(int hours, int page, int size) {
        return securityEventRepository.findRecentEvents(LocalDateTime.now().minusHours(hours), PageRequest.of(page, size));
    }

    public Page<SecurityEvent> getHighRiskEvents(int minRisk, int page, int size) {
        return securityEventRepository.findHighRiskEvents(minRisk, PageRequest.of(page, size));
    }

    public SecurityDashboardDTO getDashboard() {
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);

        long totalEvents = securityEventRepository.countByTypeSince("LOGIN_FAILURE", since24h)
                + securityEventRepository.countByTypeSince("LOGIN_SUCCESS", since24h);

        long failedLogins = securityEventRepository.countByTypeSince("LOGIN_FAILURE", since24h);
        long blockedRequests = securityEventRepository.countBlockedSince(since24h);
        long highRiskEvents = securityEventRepository.findHighRiskEvents(50, PageRequest.of(0, 1)).getTotalElements();
        long activeUsers = securityEventRepository.countDistinctUsersByTypeSince("LOGIN_SUCCESS", since24h);

        List<Object[]> topAttackIpsRaw = securityEventRepository.findTopAttackIps(
                "LOGIN_FAILURE", since24h, PageRequest.of(0, 10));
        List<SecurityDashboardDTO.TopIpEntry> topAttackIps = topAttackIpsRaw.stream()
                .map(row -> new SecurityDashboardDTO.TopIpEntry((String) row[0], (Long) row[1]))
                .toList();

        Page<SecurityEvent> recentPage = securityEventRepository.findRecentEvents(since24h, PageRequest.of(0, 20));
        List<SecurityEventDTO> recentEvents = recentPage.getContent().stream()
                .map(this::toDto)
                .toList();

        return new SecurityDashboardDTO(
                totalEvents,
                failedLogins,
                blockedRequests,
                highRiskEvents,
                0,
                activeUsers,
                topAttackIps,
                recentEvents
        );
    }

    private SecurityEventDTO toDto(SecurityEvent event) {
        return new SecurityEventDTO(
                event.getId(),
                event.getEventType(),
                event.getUserId(),
                event.getUserEmail(),
                event.getIpAddress(),
                event.getUserAgent(),
                event.getDeviceFingerprint(),
                event.getGeoLocation(),
                event.getDetails(),
                event.getRiskScore(),
                event.isBlocked(),
                event.getCreatedAt()
        );
    }
}
