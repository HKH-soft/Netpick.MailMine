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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final DelegatedAuthEntryPoint delegatedAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/auth/sign-up",
                        "/api/v1/auth/sign-in",
                        "/api/v1/auth/verify",
                        "/api/v1/auth/resend-verification",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/**")
                .permitAll()
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
