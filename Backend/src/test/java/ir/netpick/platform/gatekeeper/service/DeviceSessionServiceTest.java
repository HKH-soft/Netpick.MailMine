package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.dto.DeviceSessionDTO;
import ir.netpick.platform.gatekeeper.model.DeviceSession;
import ir.netpick.platform.gatekeeper.model.RefreshToken;
import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.DeviceSessionRepository;
import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceSessionServiceTest {

    @Mock
    private DeviceSessionRepository deviceSessionRepository;

    @Mock
    private SecurityEventService securityEventService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private DeviceSessionService deviceSessionService;

    private UUID testUserId;
    private UUID testSessionId;
    private UUID testTokenId;
    private User testUser;
    private RefreshToken testRefreshToken;
    private DeviceSession testSession;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testSessionId = UUID.randomUUID();
        testTokenId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("session@test.com");

        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(testTokenId);
        testRefreshToken.setUser(testUser);
        testRefreshToken.setToken("test-token");

        testSession = new DeviceSession();
        testSession.setId(testSessionId);
        testSession.setUser(testUser);
        testSession.setRefreshToken(testRefreshToken);
        testSession.setRevoked(false);
        testSession.setLastActiveAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createSession Tests")
    class CreateSessionTests {
        @Test
        @DisplayName("Should create session when under max limit")
        void shouldCreateSessionUnderLimit() {
            when(httpRequest.getHeader(anyString())).thenReturn("TestAgent");
            when(deviceSessionRepository.countByUserIdAndRevokedFalseAndDeletedFalse(testUserId)).thenReturn(2L);
            when(deviceSessionRepository.save(any())).thenAnswer(inv -> {
                DeviceSession ds = inv.getArgument(0);
                ds.setId(testSessionId);
                return ds;
            });

            DeviceSession result = deviceSessionService.createSession(
                    testUser, testRefreshToken, "device-info", "127.0.0.1", httpRequest);

            assertNotNull(result);
            assertEquals(testUser, result.getUser());
        }

        @Test
        @DisplayName("Should revoke oldest session when at max limit")
        void shouldRevokeOldestWhenAtMax() {
            when(httpRequest.getHeader(anyString())).thenReturn("TestAgent");
            when(deviceSessionRepository.countByUserIdAndRevokedFalseAndDeletedFalse(testUserId)).thenReturn(5L);
            when(deviceSessionRepository.findActiveSessionsByUserId(testUserId)).thenReturn(List.of(testSession));
            when(deviceSessionRepository.save(any())).thenAnswer(inv -> {
                DeviceSession ds = inv.getArgument(0);
                ds.setId(testSessionId);
                return ds;
            });
            when(deviceSessionRepository.revokeById(any(UUID.class))).thenReturn(1);

            DeviceSession result = deviceSessionService.createSession(
                    testUser, testRefreshToken, "device-info", "127.0.0.1", httpRequest);

            verify(deviceSessionRepository).revokeById(any(UUID.class));
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("touchSession Tests")
    class TouchSessionTests {
        @Test
        @DisplayName("Should update last active time")
        void shouldTouchSession() {
            deviceSessionService.touchSession(testSessionId);

            verify(deviceSessionRepository).updateLastActive(eq(testSessionId), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("getActiveSessions Tests")
    class GetActiveSessionsTests {
        @Test
        @DisplayName("Should return sessions excluding revoked")
        void shouldReturnActiveSessions() {
            DeviceSession activeSession = new DeviceSession();
            activeSession.setId(testSessionId);
            activeSession.setUser(testUser);

            when(deviceSessionRepository.findActiveSessionsByUserId(testUserId)).thenReturn(List.of(activeSession));

            List<DeviceSessionDTO> result = deviceSessionService.getActiveSessions(testUserId, testSessionId);

            assertEquals(1, result.size());
            assertTrue(result.get(0).currentSession());
        }
    }

    @Nested
    @DisplayName("revokeSession Tests")
    class RevokeSessionTests {
        @Test
        @DisplayName("Should throw when session not found")
        void shouldThrowWhenSessionNotFound() {
            when(deviceSessionRepository.findById(testSessionId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> deviceSessionService.revokeSession(testUserId, testSessionId, "user"));
        }

        @Test
        @DisplayName("Should throw when session belongs to another user")
        void shouldThrowWhenSessionBelongsToAnotherUser() {
            UUID otherUserId = UUID.randomUUID();
            DeviceSession otherSession = new DeviceSession();
            otherSession.setId(testSessionId);
            otherSession.setUser(new User());
            otherSession.getUser().setId(otherUserId);

            when(deviceSessionRepository.findById(testSessionId)).thenReturn(Optional.of(otherSession));

            assertThrows(RequestValidationException.class, 
                    () -> deviceSessionService.revokeSession(testUserId, testSessionId, "user"));
        }

        @Test
        @DisplayName("Should revoke session")
        void shouldRevokeSession() {
            when(deviceSessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
            when(deviceSessionRepository.revokeById(testSessionId)).thenReturn(1);

            deviceSessionService.revokeSession(testUserId, testSessionId, "user");

            verify(securityEventService).logEventSync(any(SecurityEvent.EventType.class),
                    any(UUID.class), anyString(), nullable(String.class), nullable(String.class),
                    nullable(String.class),
                    any(), anyInt(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("revokeAllSessions Tests")
    class RevokeAllSessionsTests {
        @Test
        @DisplayName("Should revoke all user sessions")
        void shouldRevokeAllSessions() {
            when(deviceSessionRepository.revokeAllByUserId(testUserId)).thenReturn(3);

            deviceSessionService.revokeAllSessions(testUserId);

            verify(securityEventService).logEventSync(any(SecurityEvent.EventType.class),
                    any(UUID.class), nullable(String.class), nullable(String.class), nullable(String.class),
                    nullable(String.class),
                    any(), anyInt(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("cleanupExpiredSessions Tests")
    class CleanupExpiredSessionsTests {
        @Test
        @DisplayName("Should cleanup expired sessions")
        void shouldCleanupExpiredSessions() {
            when(deviceSessionRepository.cleanupExpiredSessions()).thenReturn(5);

            deviceSessionService.cleanupExpiredSessions();

            verify(deviceSessionRepository).cleanupExpiredSessions();
        }
    }
}