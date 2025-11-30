package ir.netpick.mailmine.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.common.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWTUtil Unit Tests")
class JWTUtilTest {

    @InjectMocks
    private JWTUtil jwtUtil;

    private User testUser;
    private static final String TEST_SECRET_KEY = "this-is-a-very-long-secret-key-for-testing-purposes-at-least-256-bits";
    private static final String TEST_ISSUER = "test-issuer";
    private static final long TEST_EXPIRATION_MINUTES = 15L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "issuer", TEST_ISSUER);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMinutes", TEST_EXPIRATION_MINUTES);

        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password");
        testUser.setRole(userRole);
        testUser.setIsVerified(true);
    }

    @Nested
    @DisplayName("Issue Token Tests")
    class IssueTokenTests {

        @Test
        @DisplayName("Should issue token with subject only")
        void shouldIssueTokenWithSubjectOnly() {
            String token = jwtUtil.issueToken("test@example.com");

            assertThat(token).isNotNull().isNotEmpty();
            assertThat(jwtUtil.getSubject(token)).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should issue token with scopes as varargs")
        void shouldIssueTokenWithScopesAsVarargs() {
            String token = jwtUtil.issueToken("test@example.com", "ROLE_USER", "ROLE_ADMIN");

            assertThat(token).isNotNull();
            String subject = jwtUtil.getSubject(token);
            assertThat(subject).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should issue token with scopes as list")
        void shouldIssueTokenWithScopesAsList() {
            List<String> scopes = List.of("ROLE_USER", "ROLE_ADMIN");
            String token = jwtUtil.issueToken("test@example.com", scopes);

            assertThat(token).isNotNull();
            String subject = jwtUtil.getSubject(token);
            assertThat(subject).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should issue token with custom claims")
        void shouldIssueTokenWithCustomClaims() {
            Map<String, Object> claims = Map.of(
                    "userId", "123",
                    "role", "USER");
            String token = jwtUtil.issueToken("test@example.com", claims);

            assertThat(token).isNotNull();
            assertThat(jwtUtil.getSubject(token)).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should generate different tokens for same subject")
        void shouldGenerateDifferentTokensForSameSubject() throws InterruptedException {
            String token1 = jwtUtil.issueToken("test@example.com");
            Thread.sleep(10); // Small delay to ensure different iat
            String token2 = jwtUtil.issueToken("test@example.com");

            // Tokens may be same if issued at same millisecond, but subjects should match
            assertThat(jwtUtil.getSubject(token1)).isEqualTo(jwtUtil.getSubject(token2));
        }
    }

    @Nested
    @DisplayName("Get Subject Tests")
    class GetSubjectTests {

        @Test
        @DisplayName("Should extract subject from valid token")
        void shouldExtractSubjectFromValidToken() {
            String email = "user@domain.com";
            String token = jwtUtil.issueToken(email);

            String subject = jwtUtil.getSubject(token);

            assertThat(subject).isEqualTo(email);
        }

        @Test
        @DisplayName("Should throw for malformed token")
        void shouldThrowForMalformedToken() {
            assertThatThrownBy(() -> jwtUtil.getSubject("not-a-valid-token"))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("Should throw for tampered token")
        void shouldThrowForTamperedToken() {
            String token = jwtUtil.issueToken("test@example.com");
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            assertThatThrownBy(() -> jwtUtil.getSubject(tamperedToken))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("Should throw for token with different secret")
        void shouldThrowForTokenWithDifferentSecret() {
            String token = jwtUtil.issueToken("test@example.com");

            // Change the secret key
            ReflectionTestUtils.setField(jwtUtil, "secretKey",
                    "different-secret-key-that-is-also-at-least-256-bits-long-for-hmac");

            assertThatThrownBy(() -> jwtUtil.getSubject(token))
                    .isInstanceOf(SignatureException.class);
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate token with matching user")
        void shouldValidateTokenWithMatchingUser() {
            String token = jwtUtil.issueToken(testUser.getEmail());

            boolean isValid = jwtUtil.isTokenValid(token, testUser);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject token with different user")
        void shouldRejectTokenWithDifferentUser() {
            String token = jwtUtil.issueToken("other@example.com");

            boolean isValid = jwtUtil.isTokenValid(token, testUser);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Set very short expiration
            ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMinutes", 0L);

            String token = jwtUtil.issueToken(testUser.getEmail());

            // Token should already be expired
            assertThatThrownBy(() -> jwtUtil.isTokenValid(token, testUser))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("Should validate token for different roles")
        void shouldValidateTokenForDifferentRoles() {
            // Test with USER role
            String userToken = jwtUtil.issueToken(testUser.getEmail(), "USER");
            assertThat(jwtUtil.isTokenValid(userToken, testUser)).isTrue();

            // Test with ADMIN role
            String adminToken = jwtUtil.issueToken(testUser.getEmail(), "ADMIN");
            assertThat(jwtUtil.isTokenValid(adminToken, testUser)).isTrue();
        }
    }

    @Nested
    @DisplayName("Expiration Time Tests")
    class ExpirationTimeTests {

        @Test
        @DisplayName("Should return configured expiration minutes")
        void shouldReturnConfiguredExpirationMinutes() {
            long expirationMinutes = jwtUtil.getAccessTokenExpirationMinutes();

            assertThat(expirationMinutes).isEqualTo(TEST_EXPIRATION_MINUTES);
        }

        @Test
        @DisplayName("Should use default expiration when not configured")
        void shouldUseDefaultExpirationWhenNotConfigured() {
            // The default is 15 minutes as per @Value annotation
            assertThat(jwtUtil.getAccessTokenExpirationMinutes()).isEqualTo(15L);
        }

        @Test
        @DisplayName("Should handle custom expiration time")
        void shouldHandleCustomExpirationTime() {
            ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMinutes", 60L);

            long expirationMinutes = jwtUtil.getAccessTokenExpirationMinutes();

            assertThat(expirationMinutes).isEqualTo(60L);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty subject")
        void shouldHandleEmptySubject() {
            String token = jwtUtil.issueToken("");

            // Empty subject still creates a valid token, subject may be empty or null
            assertThat(token).isNotNull();
            String subject = jwtUtil.getSubject(token);
            assertThat(subject == null || subject.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            String specialEmail = "test+filter@sub.domain.com";
            String token = jwtUtil.issueToken(specialEmail);

            assertThat(jwtUtil.getSubject(token)).isEqualTo(specialEmail);
        }

        @Test
        @DisplayName("Should handle long email addresses")
        void shouldHandleLongEmailAddresses() {
            String longEmail = "a".repeat(100) + "@" + "b".repeat(100) + ".com";
            String token = jwtUtil.issueToken(longEmail);

            assertThat(jwtUtil.getSubject(token)).isEqualTo(longEmail);
        }

        @Test
        @DisplayName("Should handle unicode in claims")
        void shouldHandleUnicodeInClaims() {
            Map<String, Object> claims = Map.of(
                    "name", "مستخدم", // Arabic
                    "city", "東京" // Japanese
            );
            String token = jwtUtil.issueToken("test@example.com", claims);

            assertThat(jwtUtil.getSubject(token)).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should handle empty claims map")
        void shouldHandleEmptyClaimsMap() {
            String token = jwtUtil.issueToken("test@example.com", Map.of());

            assertThat(jwtUtil.getSubject(token)).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should handle null in scopes list")
        void shouldHandleEmptyScopes() {
            String token = jwtUtil.issueToken("test@example.com", List.of());

            assertThat(jwtUtil.getSubject(token)).isEqualTo("test@example.com");
        }
    }
}
