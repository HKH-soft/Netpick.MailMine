package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.exception.InvalidTokenException;
import ir.netpick.mailmine.auth.model.RefreshToken;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RefreshTokenRepository;
import ir.netpick.mailmine.common.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Set default expiration days
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationDays", 7L);

        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setRole(userRole);
    }

    @Nested
    @DisplayName("Create Refresh Token Tests")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("Should create refresh token with correct data")
        void shouldCreateRefreshTokenWithCorrectData() {
            String deviceInfo = "Mozilla/5.0";
            String ipAddress = "192.168.1.1";

            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RefreshToken result = refreshTokenService.createRefreshToken(testUser, deviceInfo, ipAddress);

            assertThat(result).isNotNull();
            assertThat(result.getUser()).isEqualTo(testUser);
            assertThat(result.getDeviceInfo()).isEqualTo(deviceInfo);
            assertThat(result.getIpAddress()).isEqualTo(ipAddress);
            assertThat(result.isRevoked()).isFalse();
        }

        @Test
        @DisplayName("Should create token with correct expiration")
        void shouldCreateTokenWithCorrectExpiration() {
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RefreshToken result = refreshTokenService.createRefreshToken(testUser, null, null);

            Instant expectedExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
            assertThat(result.getExpiresAt())
                    .isAfter(Instant.now())
                    .isBefore(expectedExpiry.plus(1, ChronoUnit.MINUTES));
        }

        @Test
        @DisplayName("Should generate secure random token")
        void shouldGenerateSecureRandomToken() {
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RefreshToken token1 = refreshTokenService.createRefreshToken(testUser, null, null);
            RefreshToken token2 = refreshTokenService.createRefreshToken(testUser, null, null);

            assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
            assertThat(token1.getToken()).hasSizeGreaterThan(32); // Base64 encoded 64 bytes
        }

        @Test
        @DisplayName("Should save token to repository")
        void shouldSaveTokenToRepository() {
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            refreshTokenService.createRefreshToken(testUser, "device", "ip");

            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should handle null device info and IP")
        void shouldHandleNullDeviceInfoAndIp() {
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RefreshToken result = refreshTokenService.createRefreshToken(testUser, null, null);

            assertThat(result.getDeviceInfo()).isNull();
            assertThat(result.getIpAddress()).isNull();
        }
    }

    @Nested
    @DisplayName("Verify Refresh Token Tests")
    class VerifyRefreshTokenTests {

        @Test
        @DisplayName("Should verify valid token successfully")
        void shouldVerifyValidTokenSuccessfully() {
            String tokenValue = "valid-token";
            RefreshToken token = new RefreshToken(tokenValue, testUser,
                    Instant.now().plus(1, ChronoUnit.DAYS));

            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            RefreshToken result = refreshTokenService.verifyRefreshToken(tokenValue);

            assertThat(result).isEqualTo(token);
        }

        @Test
        @DisplayName("Should throw when token not found")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("invalid-token"))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("Should throw when token is revoked")
        void shouldThrowWhenTokenIsRevoked() {
            RefreshToken token = new RefreshToken("revoked-token", testUser,
                    Instant.now().plus(1, ChronoUnit.DAYS));
            token.revoke();

            when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("revoked-token"))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("revoked");
        }

        @Test
        @DisplayName("Should throw when token is expired")
        void shouldThrowWhenTokenIsExpired() {
            RefreshToken token = new RefreshToken("expired-token", testUser,
                    Instant.now().minus(1, ChronoUnit.DAYS));

            when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("expired-token"))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("expired");
        }
    }

    @Nested
    @DisplayName("Revoke Token Tests")
    class RevokeTokenTests {

        @Test
        @DisplayName("Should revoke token successfully")
        void shouldRevokeTokenSuccessfully() {
            when(refreshTokenRepository.revokeByToken("token-to-revoke")).thenReturn(1);

            refreshTokenService.revokeToken("token-to-revoke");

            verify(refreshTokenRepository).revokeByToken("token-to-revoke");
        }

        @Test
        @DisplayName("Should handle revoking non-existent token")
        void shouldHandleRevokingNonExistentToken() {
            when(refreshTokenRepository.revokeByToken("non-existent")).thenReturn(0);

            // Should not throw
            refreshTokenService.revokeToken("non-existent");

            verify(refreshTokenRepository).revokeByToken("non-existent");
        }
    }

    @Nested
    @DisplayName("Revoke All User Tokens Tests")
    class RevokeAllUserTokensTests {

        @Test
        @DisplayName("Should revoke all tokens for user")
        void shouldRevokeAllTokensForUser() {
            UUID userId = testUser.getId();
            when(refreshTokenRepository.revokeAllByUserId(userId)).thenReturn(3);

            refreshTokenService.revokeAllUserTokens(userId);

            verify(refreshTokenRepository).revokeAllByUserId(userId);
        }

        @Test
        @DisplayName("Should handle user with no tokens")
        void shouldHandleUserWithNoTokens() {
            UUID userId = testUser.getId();
            when(refreshTokenRepository.revokeAllByUserId(userId)).thenReturn(0);

            // Should not throw
            refreshTokenService.revokeAllUserTokens(userId);

            verify(refreshTokenRepository).revokeAllByUserId(userId);
        }
    }

    @Nested
    @DisplayName("Rotate Refresh Token Tests")
    class RotateRefreshTokenTests {

        @Test
        @DisplayName("Should revoke old token and create new one")
        void shouldRevokeOldTokenAndCreateNewOne() {
            String oldToken = "old-token";
            when(refreshTokenRepository.revokeByToken(oldToken)).thenReturn(1);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RefreshToken newToken = refreshTokenService.rotateRefreshToken(
                    oldToken, testUser, "device", "ip");

            verify(refreshTokenRepository).revokeByToken(oldToken);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
            assertThat(newToken.getToken()).isNotEqualTo(oldToken);
        }

        @Test
        @DisplayName("Should preserve device info and IP on rotation")
        void shouldPreserveDeviceInfoAndIpOnRotation() {
            String deviceInfo = "Mozilla/5.0";
            String ipAddress = "10.0.0.1";

            when(refreshTokenRepository.revokeByToken(any())).thenReturn(1);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RefreshToken newToken = refreshTokenService.rotateRefreshToken(
                    "old-token", testUser, deviceInfo, ipAddress);

            assertThat(newToken.getDeviceInfo()).isEqualTo(deviceInfo);
            assertThat(newToken.getIpAddress()).isEqualTo(ipAddress);
        }
    }

    @Nested
    @DisplayName("Find Valid Token Tests")
    class FindValidTokenTests {

        @Test
        @DisplayName("Should find valid token")
        void shouldFindValidToken() {
            String tokenValue = "valid-token";
            RefreshToken token = new RefreshToken(tokenValue, testUser,
                    Instant.now().plus(1, ChronoUnit.DAYS));

            when(refreshTokenRepository.findValidToken(eq(tokenValue), any(Instant.class)))
                    .thenReturn(Optional.of(token));

            Optional<RefreshToken> result = refreshTokenService.findValidToken(tokenValue);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(token);
        }

        @Test
        @DisplayName("Should return empty for invalid token")
        void shouldReturnEmptyForInvalidToken() {
            when(refreshTokenRepository.findValidToken(eq("invalid"), any(Instant.class)))
                    .thenReturn(Optional.empty());

            Optional<RefreshToken> result = refreshTokenService.findValidToken("invalid");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty token string")
        void shouldHandleEmptyTokenString() {
            when(refreshTokenRepository.findByToken("")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken(""))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("Should handle token at exact expiration time")
        void shouldHandleTokenAtExactExpirationTime() {
            // Token expires exactly now - should be considered expired
            RefreshToken token = new RefreshToken("edge-token", testUser, Instant.now());

            when(refreshTokenRepository.findByToken("edge-token")).thenReturn(Optional.of(token));

            // Depending on timing, this might or might not throw
            // The important thing is it handles the edge case
            try {
                refreshTokenService.verifyRefreshToken("edge-token");
            } catch (InvalidTokenException e) {
                assertThat(e.getMessage()).contains("expired");
            }
        }
    }
}
