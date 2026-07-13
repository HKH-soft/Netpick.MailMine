package ir.netpick.platform.mailmine.dto;

import ir.netpick.platform.core.enums.ProxyProtocol;
import ir.netpick.platform.core.enums.ProxyStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProxyResponse(
        UUID id,
        ProxyProtocol protocol,
        String host,
        Integer port,
        String username,
        ProxyStatus status,
        LocalDateTime lastTestedAt,
        LocalDateTime lastUsedAt,
        Integer successCount,
        Integer failureCount,
        Long avgResponseTimeMs,
        String description,
        LocalDateTime createdAt,
        // V2Ray specific fields
        String uuid,
        String encryption,
        String transport,
        String security,
        String sni,
        Integer localPort,
        boolean isV2Ray,
        // Vercel Relay fields
        String vercelToken,
        String relaySessionId) {
}









