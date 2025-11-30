package ir.netpick.mailmine.scrape.dto;

import ir.netpick.mailmine.common.enums.ProxyProtocol;
import ir.netpick.mailmine.common.enums.ProxyStatus;

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
        boolean isV2Ray) {
}
