package ir.netpick.platform.gatekeeper.security;

import ir.netpick.platform.gatekeeper.service.IpPolicyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class IpPolicyFilter extends OncePerRequestFilter {

    private final IpPolicyService ipPolicyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = extractClientIp(request);
        IpPolicyService.IpAccessResult result = ipPolicyService.checkAccess(clientIp);

        if (!result.allowed()) {
            log.warn("IP BLOCKED: {} on {} - reason: {}", clientIp, request.getRequestURI(), result.reason());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied\",\"reason\":\"IP address is blocked\"}");
            return;
        }

        request.setAttribute("client.ip", clientIp);
        filterChain.doFilter(request, response);
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
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
