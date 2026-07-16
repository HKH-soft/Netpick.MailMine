package ir.netpick.platform.core;

import org.springframework.context.annotation.Configuration;

/**
 * CORS is configured in gatekeeper.security.CorsConfig via SecurityFilterChain.
 * This class is intentionally left empty to avoid duplicate CORS filter beans.
 * The gatekeeper module's CorsConfig handles all CORS configuration.
 */
@Configuration
public class CorsConfigEnabled {
    // Intentionally empty - CORS is configured in gatekeeper.security.CorsConfig via SecurityFilterChainConfig
}