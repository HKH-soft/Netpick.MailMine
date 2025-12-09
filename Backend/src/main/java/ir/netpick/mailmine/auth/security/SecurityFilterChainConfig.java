package ir.netpick.mailmine.auth.security;

import ir.netpick.mailmine.auth.jwt.JWTAuthenticationFilter;
import ir.netpick.mailmine.common.exception.DelegatedAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
                http.csrf(AbstractHttpConfigurer::disable);
                http.authorizeHttpRequests(request -> request
                                // Allow preflight OPTIONS requests for CORS
                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                                "/api/v1/auth/sign-up",
                                                "/api/v1/auth/sign-in",
                                                "/api/v1/auth/verify",
                                                "/api/v1/auth/resend-verification",
                                                "/api/v1/auth/refresh",
                                                "/api/v1/auth/logout")
                                .permitAll()
                                // Actuator endpoints require authentication for security (except health checks)
                                .requestMatchers("/actuator/health", "/actuator/health/**")
                                .permitAll()
                                .requestMatchers("/actuator/**")
                                .authenticated()
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
                return http.build();
        }

}
