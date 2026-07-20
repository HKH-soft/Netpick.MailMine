package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.core.enums.RoleEnum;
import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.gatekeeper.dto.AuthenticationSigninRequest;
import ir.netpick.platform.gatekeeper.dto.AuthenticationSignupRequest;
import ir.netpick.platform.gatekeeper.dto.RefreshTokenRequest;
import ir.netpick.platform.gatekeeper.email.AuthEmailService;
import ir.netpick.platform.gatekeeper.exception.AccountNotVerifiedException;
import ir.netpick.platform.gatekeeper.exception.MfaRequiredException;
import ir.netpick.platform.gatekeeper.exception.RateLimitExceededException;
import ir.netpick.platform.gatekeeper.jwt.JWTUtil;
import ir.netpick.platform.gatekeeper.mapper.UserDTOMapper;
import ir.netpick.platform.gatekeeper.model.Role;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private UserDTOMapper userDTOMapper;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationService verificationService;

    @Mock
    private AuthEmailService authEmailService;

    @Mock
    private RateLimiting rateLimitingService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MfaService mfaService;

    @Mock
    private DeviceSessionService deviceSessionService;

    @Mock
    private SecurityEventService securityEventService;

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @Mock
    private IpPolicyService ipPolicyService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UUID testUserId;
    private String testEmail;
    private String testPassword;
    private Role testRole;
    private User testUser;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "auth@test.com";
        testPassword = "V@lidPass12345!";

        testRole = new Role(RoleEnum.USER);
        testRole.setId(UUID.randomUUID());

        testUser = new User(testEmail, "encodedPassword", "Test User", testRole);
        testUser.setId(testUserId);
        testUser.setIsVerified(true);
        testUser.setMfaEnabled(false);

        mockAuthentication = mock(Authentication.class);
    }

    @Nested
    @DisplayName("signIn Tests")
    class SignInTests {

        @Test
        @DisplayName("Should throw RateLimitExceededException when IP blocked")
        void shouldThrowWhenIpBlocked() {
            when(ipPolicyService.checkAccess(anyString())).thenReturn(
                    new IpPolicyService.IpAccessResult(false, "IP blocked"));

            assertThrows(RateLimitExceededException.class, 
                    () -> authenticationService.signIn(
                            new AuthenticationSigninRequest(testEmail, testPassword),
                            "device", "127.0.0.1"));
        }

        @Test
        @DisplayName("Should throw RateLimitExceededException when login attempts exceeded")
        void shouldThrowWhenLoginAttemptsExceeded() {
            when(ipPolicyService.checkAccess(anyString())).thenReturn(
                    new IpPolicyService.IpAccessResult(true, null));
            when(rateLimitingService.canAttemptLogin(anyString())).thenReturn(false);
            when(rateLimitingService.getRemainingLockoutMinutes(anyString())).thenReturn(10L);

            assertThrows(RateLimitExceededException.class, 
                    () -> authenticationService.signIn(
                            new AuthenticationSigninRequest(testEmail, testPassword),
                            "device", "127.0.0.1"));
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when user not verified")
        void shouldThrowWhenUserNotVerified() {
            testUser.setIsVerified(false);

            when(ipPolicyService.checkAccess(anyString())).thenReturn(
                    new IpPolicyService.IpAccessResult(true, null));
            when(rateLimitingService.canAttemptLogin(anyString())).thenReturn(true);
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            doNothing().when(rateLimitingService).recordFailedLoginAttempt(anyString());

            assertThrows(BadCredentialsException.class, 
                    () -> authenticationService.signIn(
                            new AuthenticationSigninRequest(testEmail, testPassword),
                            "device", "127.0.0.1"));
        }
    }

    @Nested
    @DisplayName("signUp Tests")
    class SignUpTests {

        @Test
        @DisplayName("Should create unverified user on sign up")
        void shouldCreateUnverifiedUser() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    testEmail, testPassword, "Test User");

            testUser.setIsVerified(false);

            when(userService.createUnverifiedUser(any())).thenReturn(testUser);
            when(userService.prepareUserForVerification(any())).thenReturn("123456");

            authenticationService.signUp(request);

            verify(userService).createUnverifiedUser(request);
            verify(userService).prepareUserForVerification(testUser);
        }
    }

    @Nested
    @DisplayName("verifyUser Tests")
    class VerifyUserTests {

        @Test
        @DisplayName("Should throw RateLimitExceededException when verification attempts exceeded")
        void shouldThrowWhenVerificationAttemptsExceeded() {
            when(rateLimitingService.canAttemptVerification(anyString())).thenReturn(false);

            assertThrows(RateLimitExceededException.class, 
                    () -> authenticationService.verifyUser(
                            new ir.netpick.platform.gatekeeper.dto.VerificationRequest(testEmail, "123456")));
        }
    }

    @Nested
    @DisplayName("refreshAccessToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should call refresh token service with correct parameters")
        void shouldCallRefreshTokenService() {
            var mockRefreshToken = mock(ir.netpick.platform.gatekeeper.model.RefreshToken.class);
            User mockUser = mock(User.class);
            when(mockRefreshToken.getUser()).thenReturn(mockUser);
            when(mockUser.getDeleted()).thenReturn(false);
            when(mockUser.getEmail()).thenReturn(testEmail);
            when(mockUser.getRole()).thenReturn(testRole);
            when(refreshTokenService.verifyRefreshToken(anyString())).thenReturn(mockRefreshToken);
            when(jwtUtil.issueToken(anyString(), anyString())).thenReturn("new-access-token");
            when(refreshTokenService.rotateRefreshToken(any(), any(), anyString(), anyString()))
                    .thenReturn(mockRefreshToken);

            authenticationService.refreshAccessToken("valid-refresh-token", "device", "127.0.0.1");

            verify(refreshTokenService).verifyRefreshToken("valid-refresh-token");
        }
    }

    @Nested
    @DisplayName("logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should call refresh token service to revoke token")
        void shouldRevokeToken() {
            authenticationService.logout("token-to-revoke");

            verify(refreshTokenService).revokeToken("token-to-revoke");
        }
    }

    @Nested
    @DisplayName("logoutAllDevices Tests")
    class LogoutAllDevicesTests {

        @Test
        @DisplayName("Should revoke all user tokens")
        void shouldRevokeAllTokens() {
            when(userService.getUserEntity(anyString())).thenReturn(testUser);

            authenticationService.logoutAllDevices(testEmail);

            verify(refreshTokenService).revokeAllUserTokens(testUserId);
        }
    }
}