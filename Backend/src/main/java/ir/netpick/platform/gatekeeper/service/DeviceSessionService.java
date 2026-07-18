package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.dto.DeviceSessionDTO;
import ir.netpick.platform.gatekeeper.model.DeviceSession;
import ir.netpick.platform.gatekeeper.model.RefreshToken;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.DeviceSessionRepository;
import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceSessionService {

    private final DeviceSessionRepository deviceSessionRepository;
    private final SecurityEventService securityEventService;

    @Value("${security.sessions.max-concurrent:5}")
    private int maxConcurrentSessions;

    @Value("${security.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    @Transactional
    public DeviceSession createSession(User user, RefreshToken refreshToken, String deviceInfo,
                                       String ipAddress, HttpServletRequest request) {
        long activeCount = deviceSessionRepository.countByUserIdAndRevokedFalseAndDeletedFalse(user.getId());

        if (activeCount >= maxConcurrentSessions) {
            revokeOldestSession(user.getId());
        }

        String fingerprint = extractFingerprint(request);

        DeviceSession session = new DeviceSession();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setDeviceInfo(deviceInfo);
        session.setDeviceFingerprint(fingerprint);
        session.setIpAddress(ipAddress);
        session.setExpiresAt(Instant.now().plusSeconds(refreshExpirationDays * 86400));
        session.setLastActiveAt(LocalDateTime.now());

        DeviceSession saved = deviceSessionRepository.save(session);

        securityEventService.logEventSync(
                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.SESSION_CREATED,
                user.getId(), user.getEmail(), ipAddress, deviceInfo, fingerprint,
                java.util.Map.of("sessionId", saved.getId().toString(), "activeSessions", activeCount + 1),
                0, false);

        log.info("Session created for user: {} from IP: {}", user.getEmail(), ipAddress);
        return saved;
    }

    @Transactional
    public void touchSession(UUID sessionId) {
        deviceSessionRepository.updateLastActive(sessionId, LocalDateTime.now());
    }

    public List<DeviceSessionDTO> getActiveSessions(UUID userId, UUID currentSessionId) {
        List<DeviceSession> sessions = deviceSessionRepository.findActiveSessionsByUserId(userId);
        return sessions.stream()
                .map(s -> new DeviceSessionDTO(
                        s.getId(),
                        s.getDeviceInfo(),
                        s.getDeviceFingerprint(),
                        s.getIpAddress(),
                        s.getGeoLocation(),
                        s.getLastActiveAt(),
                        s.getExpiresAt(),
                        s.isRevoked(),
                        s.getId().equals(currentSessionId),
                        s.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId, String requestedBy) {
        DeviceSession session = deviceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new RequestValidationException("Session does not belong to this user");
        }

        deviceSessionRepository.revokeById(sessionId);

        securityEventService.logEventSync(
                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.SESSION_REVOKED,
                userId, requestedBy, session.getIpAddress(), session.getDeviceInfo(),
                session.getDeviceFingerprint(),
                java.util.Map.of("revokedSessionId", sessionId.toString()), 0, false);

        log.info("Session {} revoked for user: {}", sessionId, requestedBy);
    }

    @Transactional
    public void revokeAllSessions(UUID userId) {
        int count = deviceSessionRepository.revokeAllByUserId(userId);

        securityEventService.logEventSync(
                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.LOGOUT_ALL_DEVICES,
                userId, null, null, null, null,
                java.util.Map.of("revokedCount", count), 0, false);

        log.info("Revoked {} sessions for user: {}", count, userId);
    }

    private void revokeOldestSession(UUID userId) {
        List<DeviceSession> sessions = deviceSessionRepository.findActiveSessionsByUserId(userId);
        if (!sessions.isEmpty()) {
            DeviceSession oldest = sessions.get(sessions.size() - 1);
            deviceSessionRepository.revokeById(oldest.getId());
            log.debug("Revoked oldest session {} to make room for new session", oldest.getId());
        }
    }

    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional
    public void cleanupExpiredSessions() {
        int count = deviceSessionRepository.cleanupExpiredSessions();
        if (count > 0) {
            log.info("Cleaned up {} expired/revoked sessions", count);
        }
    }

    private String extractFingerprint(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        String accept = request.getHeader("Accept-Language");
        String encoding = request.getHeader("Accept-Encoding");
        String combined = (ua != null ? ua : "") + "|" +
                (accept != null ? accept : "") + "|" +
                (encoding != null ? encoding : "");
        // Use SHA-256 for more stable fingerprint instead of hashCode
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(combined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(combined.hashCode());
        }
    }
}
