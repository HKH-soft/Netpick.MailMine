package ir.netpick.platform.gatekeeper.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("#{'${cors.allowed-origins:}'.split(',')}")
    private List<String> allowedOrigins;

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Remove empty strings from the list
        List<String> origins = allowedOrigins.stream()
                .filter(s -> s != null && !s.isEmpty())
                .toList();

        // Fail closed: if no origins configured, use same-origin only
        if (origins.isEmpty()) {
            configuration.setAllowedOriginPatterns(List.of("same-origin"));
        } else {
            configuration.setAllowedOrigins(origins);
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}









