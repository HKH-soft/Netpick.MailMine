package ir.netpick.platform.core.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

/**
 * Utility for extracting the real client IP from an HTTP request.
 *
 * SECURITY NOTE: X-Forwarded-For and X-Real-IP headers are client-controllable
 * and can be spoofed. These headers are only trusted when the direct connection
 * originates from a known reverse proxy (localhost / 127.0.0.1 / ::1).
 * If the application runs behind a non-localhost proxy, configure
 * netpick.trusted-proxies with the proxy IP addresses.
 */
public final class IpUtils {

    private static volatile Set<String> TRUSTED_PROXIES = Set.of(
            "127.0.0.1", "::1", "0:0:0:0:0:0:0:1"
    );

    private IpUtils() {
    }

    /**
     * Configure trusted proxy IP addresses. Call at startup if non-localhost
     * proxies are in use.
     */
    public static void setTrustedProxies(Set<String> proxies) {
        TRUSTED_PROXIES = Set.copyOf(proxies);
    }

    /**
     * Extract the real client IP, honoring X-Forwarded-For / X-Real-IP only
     * when the direct peer is a trusted proxy.
     */
    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (!TRUSTED_PROXIES.contains(remoteAddr)) {
            return remoteAddr;
        }

        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            String clientIp = xff.split(",")[0].trim();
            if (!clientIp.isEmpty()) {
                return clientIp;
            }
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        return remoteAddr;
    }
}
