package ir.netpick.platform.gatekeeper.security;

import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import ir.netpick.platform.gatekeeper.service.SecurityEventService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class SecurityAuditFilter extends OncePerRequestFilter {

    private final SecurityEventService securityEventService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        if (status == 401 || status == 403 || status == 429) {
            String clientIp = extractClientIp(request);
            String path = request.getRequestURI();

            SecurityEvent.EventType eventType = switch (status) {
                case 401 -> SecurityEvent.EventType.LOGIN_FAILURE;
                case 403 -> SecurityEvent.EventType.IP_BLOCKED;
                case 429 -> SecurityEvent.EventType.ACCOUNT_LOCKED;
                default -> SecurityEvent.EventType.SUSPICIOUS_ACTIVITY;
            };

            securityEventService.logEventSync(
                    eventType,
                    null,
                    null,
                    clientIp,
                    request.getHeader("User-Agent"),
                    null,
                    Map.of(
                            "path", path,
                            "method", request.getMethod(),
                            "status", status,
                            "duration", duration
                    ),
                    status == 403 ? 30 : 10,
                    status == 403
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || path.equals("/actuator") ||
               path.startsWith("/swagger/") || path.equals("/swagger") ||
               path.startsWith("/v3/api-docs/");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
