package ir.netpick.platform.gatekeeper.security;

import ir.netpick.platform.gatekeeper.jwt.JWTAuthenticationFilter;
import ir.netpick.platform.core.exception.DelegatedAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {

        private final AuthenticationProvider authenticationProvider;
        private final JWTAuthenticationFilter jwtAuthenticationFilter;
        private final DelegatedAuthEntryPoint delegatedAuthEntryPoint;
        private final UrlBasedCorsConfigurationSource corsConfigurationSource;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.cors(cors -> cors.configurationSource(corsConfigurationSource));
                http.csrf(csrf -> csrf.disable());
                http.authorizeHttpRequests(request -> request
                                // Allow preflight OPTIONS requests for CORS
                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                                "/api/v1/gatekeeper/auth/sign-up",
                                                "/api/v1/gatekeeper/auth/sign-in",
                                                "/api/v1/gatekeeper/auth/verify",
                                                "/api/v1/gatekeeper/auth/resend-verification",
                                                "/api/v1/gatekeeper/auth/refresh",
                                                "/api/v1/gatekeeper/auth/logout")
                                .permitAll()
                                // Actuator endpoints - restrict to authenticated users with ADMIN role
                                .requestMatchers("/actuator/**")
                                .hasRole("ADMIN")
                                // Swagger UI and OpenAPI docs
                                .requestMatchers(
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/v3/api-docs.yaml")
                                .permitAll()
                                .anyRequest().authenticated());
                http.sessionManagement(request -> request.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                http.authenticationProvider(authenticationProvider);
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                http.exceptionHandling(request -> request.authenticationEntryPoint(delegatedAuthEntryPoint));
                // Add security headers including CSP
                http.headers(headers -> {
                        headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none';"));
                        headers.frameOptions(frame -> frame.sameOrigin());
                        // X-Content-Type-Options: nosniff is enabled by default in Spring Security 6
                });
                return http.build();
        }

}









