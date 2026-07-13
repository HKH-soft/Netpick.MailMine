package ir.netpick.mailmine.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;
    private final ObjectMapper objectMapper;

    public void log(String entityType, UUID entityId, String action,
                    UUID userId, String userEmail,
                    Object oldValue, Object newValue,
                    HttpServletRequest request) {
        try {
            AuditTrail entry = new AuditTrail();
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setAction(action);
            entry.setPerformedById(userId);
            entry.setPerformedByEmail(userEmail);

            if (oldValue != null) {
                entry.setOldValues(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                entry.setNewValues(objectMapper.writeValueAsString(newValue));
            }

            if (request != null) {
                entry.setIpAddress(getClientIp(request));
                entry.setUserAgent(request.getHeader("User-Agent"));
            }

            auditTrailRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit trail: {}", e.getMessage());
        }
    }

    public void log(String entityType, UUID entityId, String action, UUID userId, String userEmail) {
        log(entityType, entityId, action, userId, userEmail, null, null, null);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
