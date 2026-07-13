package ir.netpick.platform.core;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    @Profile("!prod") // Disable Swagger in production
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createBearerSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Netpick API")
                .description("""
                        Netpick Platform API

                        ## Extensions

                        - **Gatekeeper**: Authentication and user management (`/api/v1/gatekeeper/**`)
                        - **MailMine**: Email scraping and sending (`/api/v1/mailmine/**`)
                        - **Core**: Shared services (`/api/v1/core/**`)

                        ## Authentication

                        This API uses JWT Bearer token authentication. To authenticate:

                        1. Call `POST /api/v1/gatekeeper/auth/sign-up` to create an account
                        2. Verify your email using `POST /api/v1/gatekeeper/auth/verify`
                        3. Call `POST /api/v1/gatekeeper/auth/sign-in` to get access and refresh tokens
                        4. Use the `access_token` in the Authorization header: `Bearer <token>`
                        5. When access token expires, use `POST /api/v1/gatekeeper/auth/refresh` to get a new one

                        ## Rate Limiting

                        - Login: 5 attempts per 15 minutes
                        - Verification: 5 attempts per 10 minutes
                        - Resend verification: 3 attempts per hour
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("NetPick")
                        .email("support@netpick.ir"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://netpick.ir"));
    }

    private SecurityScheme createBearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT access token");
    }
}








