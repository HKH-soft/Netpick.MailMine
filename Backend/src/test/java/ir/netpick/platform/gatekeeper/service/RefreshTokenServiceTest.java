package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.exception.InvalidTokenException;
import ir.netpick.platform.gatekeeper.model.RefreshToken;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UUID testUserId;
    private String testEmail;
    private String testToken;
    private String testTokenHash;
    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "refresh@test.com";
        testToken = "abc123def456ghi789";
        testTokenHash = "hashed-token";

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(UUID.randomUUID());
        testRefreshToken.setToken(testToken);
        testRefreshToken.setTokenHash(testTokenHash);
        testRefreshToken.setUser(testUser);
        testRefreshToken.setExpiresAt(Instant.now().plusSeconds(86400));
    }

    @Nested
    @DisplayName("createRefreshToken Tests")
    class CreateRefreshTokenTests {
        @Test
        @DisplayName("Should create refresh token with hashed value")
        void shouldCreateTokenWithHash() {
            when(passwordEncoder.encode(anyString())).thenReturn(testTokenHash);
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken result = refreshTokenService.createRefreshToken(testUser, "device", "127.0.0.1");

            assertNotNull(result);
            assertNotNull(result.getToken());
            assertEquals(testTokenHash, result.getTokenHash());
            assertEquals(testUser, result.getUser());
        }
    }

    @Nested
    @DisplayName("verifyRefreshToken Tests")
    class VerifyRefreshTokenTests {
        @Test
        @DisplayName("Should throw InvalidTokenException when token not found")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findValidTokens(any())).thenReturn(java.util.List.of());

            assertThrows(InvalidTokenException.class, () -> refreshTokenService.verifyRefreshToken("invalid"));
        }

        @Test
        @DisplayName("Should verify valid token")
        void shouldVerifyValidToken() {
            when(refreshTokenRepository.findValidTokens(any())).thenReturn(java.util.List.of(testRefreshToken));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            RefreshToken result = refreshTokenService.verifyRefreshToken(testToken);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("revokeToken Tests")
    class RevokeTokenTests {
        @Test
        @DisplayName("Should revoke token when found")
        void shouldRevokeToken() {
            when(refreshTokenRepository.findValidTokens(any())).thenReturn(java.util.List.of(testRefreshToken));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            refreshTokenService.revokeToken(testToken);

            assertTrue(testRefreshToken.isRevoked());
            verify(refreshTokenRepository).save(testRefreshToken);
        }
    }

    @Nested
    @DisplayName("rotateRefreshToken Tests")
    class RotateTokenTests {
        @Test
        @DisplayName("Should rotate token correctly")
        void shouldRotateToken() {
            when(refreshTokenRepository.findValidTokens(any())).thenReturn(java.util.List.of(testRefreshToken));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> {
                RefreshToken rt = inv.getArgument(0);
                if (rt.isRevoked()) return rt;
                return rt;
            });

            RefreshToken result = refreshTokenService.rotateRefreshToken(testToken, testUser, "device", "127.0.0.1");

            assertNotNull(result);
        }
    }
}