package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.dto.AuthenticationResponse;
import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.dto.VerificationRequest;
import ir.netpick.mailmine.auth.email.AuthEmailService;
import ir.netpick.mailmine.auth.exception.AccountNotVerifiedException;
import ir.netpick.mailmine.auth.exception.RateLimitExceededException;
import ir.netpick.mailmine.auth.exception.UserAlreadyVerifiedException;
import ir.netpick.mailmine.auth.exception.UserNotFoundException;
import ir.netpick.mailmine.auth.exception.VerificationCodeNotFoundException;
import ir.netpick.mailmine.auth.jwt.JWTUtil;
import ir.netpick.mailmine.auth.mapper.UserDTOMapper;
import ir.netpick.mailmine.auth.model.RefreshToken;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.model.Verification;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.common.exception.VerificationException;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

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
    private RateLimitingService rateLimitingService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role userRole;
    private Role superAdminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(RoleEnum.USER);

        superAdminRole = new Role();
        superAdminRole.setName(RoleEnum.SUPER_ADMIN);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setRole(userRole);
        testUser.setIsVerified(true);
        testUser.setDeleted(false);

        // UserDTO(UUID id, String email, String name, RoleEnum role, Boolean
        // isVerified,
        // LocalDateTime created_at, LocalDateTime updatedAt, LocalDateTime lastLoginAt)
        testUserDTO = new UserDTO(
                testUser.getId(),
                testUser.getEmail(),
                "Test User",
                RoleEnum.USER,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Nested
    @DisplayName("Sign In Tests")
    class SignInTests {

        @Test
        @DisplayName("Should sign in user successfully with valid credentials")
        void shouldSignInSuccessfully() {
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("test@example.com", "password123");
            Authentication authentication = mock(Authentication.class);
            RefreshToken refreshToken = new RefreshToken("refresh-token", testUser,
                    Instant.now().plus(7, ChronoUnit.DAYS));

            when(rateLimitingService.canAttemptLogin(request.email())).thenReturn(true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(userDTOMapper.apply(testUser)).thenReturn(testUserDTO);
            when(jwtUtil.issueToken(anyString(), anyString())).thenReturn("access-token");
            when(jwtUtil.getAccessTokenExpirationMinutes()).thenReturn(15L);
            when(refreshTokenService.createRefreshToken(eq(testUser), anyString(), anyString()))
                    .thenReturn(refreshToken);

            AuthenticationResponse response = authenticationService.signIn(request, "device", "127.0.0.1");

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.expiresIn()).isEqualTo(900); // 15 * 60

            verify(rateLimitingService).clearLoginAttempts(request.email());
            verify(userService).updateLastSign(request.email());
        }

        @Test
        @DisplayName("Should throw RateLimitExceededException when too many login attempts")
        void shouldThrowWhenRateLimitExceeded() {
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("test@example.com", "password123");

            when(rateLimitingService.canAttemptLogin(request.email())).thenReturn(false);
            when(rateLimitingService.getRemainingLockoutMinutes(request.email())).thenReturn(10L);

            assertThatThrownBy(() -> authenticationService.signIn(request, null, null))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("10 minutes");
        }

        @Test
        @DisplayName("Should throw AccountNotVerifiedException for unverified user")
        void shouldThrowWhenUserNotVerified() {
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("test@example.com", "password123");
            Authentication authentication = mock(Authentication.class);
            UserDTO unverifiedUserDTO = new UserDTO(
                    testUser.getId(), testUser.getEmail(), "Test User",
                    RoleEnum.USER, false, LocalDateTime.now(), LocalDateTime.now(), null);

            when(rateLimitingService.canAttemptLogin(request.email())).thenReturn(true);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(userDTOMapper.apply(testUser)).thenReturn(unverifiedUserDTO);

            assertThatThrownBy(() -> authenticationService.signIn(request, null, null))
                    .isInstanceOf(AccountNotVerifiedException.class)
                    .hasMessageContaining("not verified");
        }

        @Test
        @DisplayName("Should allow unverified SUPER_ADMIN to sign in")
        void shouldAllowUnverifiedSuperAdmin() {
            testUser.setRole(superAdminRole);
            UserDTO superAdminDTO = new UserDTO(
                    testUser.getId(), testUser.getEmail(), "Admin User",
                    RoleEnum.SUPER_ADMIN, false, LocalDateTime.now(), LocalDateTime.now(), null);
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("admin@example.com", "password123");
            Authentication authentication = mock(Authentication.class);
            RefreshToken refreshToken = new RefreshToken("refresh-token", testUser,
                    Instant.now().plus(7, ChronoUnit.DAYS));

            when(rateLimitingService.canAttemptLogin(request.email())).thenReturn(true);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(userDTOMapper.apply(testUser)).thenReturn(superAdminDTO);
            when(jwtUtil.issueToken(anyString(), anyString())).thenReturn("access-token");
            when(jwtUtil.getAccessTokenExpirationMinutes()).thenReturn(15L);
            when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn(refreshToken);

            AuthenticationResponse response = authenticationService.signIn(request, null, null);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access-token");
        }

        @Test
        @DisplayName("Should record failed attempt on bad credentials")
        void shouldRecordFailedAttemptOnBadCredentials() {
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("test@example.com", "wrongpassword");

            when(rateLimitingService.canAttemptLogin(request.email())).thenReturn(true);
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authenticationService.signIn(request, null, null))
                    .isInstanceOf(BadCredentialsException.class);

            verify(rateLimitingService).recordFailedLoginAttempt(request.email());
        }
    }

    @Nested
    @DisplayName("Refresh Access Token Tests")
    class RefreshAccessTokenTests {

        @Test
        @DisplayName("Should refresh access token successfully")
        void shouldRefreshAccessTokenSuccessfully() {
            String oldRefreshToken = "old-refresh-token";
            RefreshToken token = new RefreshToken(oldRefreshToken, testUser,
                    Instant.now().plus(7, ChronoUnit.DAYS));
            RefreshToken newRefreshToken = new RefreshToken("new-refresh-token", testUser,
                    Instant.now().plus(7, ChronoUnit.DAYS));

            when(refreshTokenService.verifyRefreshToken(oldRefreshToken)).thenReturn(token);
            when(jwtUtil.issueToken(testUser.getEmail(), testUser.getRole().getName().toString()))
                    .thenReturn("new-access-token");
            when(jwtUtil.getAccessTokenExpirationMinutes()).thenReturn(15L);
            when(refreshTokenService.rotateRefreshToken(eq(oldRefreshToken), eq(testUser), any(), any()))
                    .thenReturn(newRefreshToken);

            AuthenticationResponse response = authenticationService.refreshAccessToken(
                    oldRefreshToken, "device", "127.0.0.1");

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(response.expiresIn()).isEqualTo(900);
        }

        @Test
        @DisplayName("Should reject refresh for deleted user")
        void shouldRejectRefreshForDeletedUser() {
            testUser.setDeleted(true);
            String refreshToken = "refresh-token";
            RefreshToken token = new RefreshToken(refreshToken, testUser,
                    Instant.now().plus(7, ChronoUnit.DAYS));

            when(refreshTokenService.verifyRefreshToken(refreshToken)).thenReturn(token);

            assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken, null, null))
                    .isInstanceOf(AccountNotVerifiedException.class)
                    .hasMessageContaining("deactivated");

            verify(refreshTokenService).revokeToken(refreshToken);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout user by revoking refresh token")
        void shouldLogoutSuccessfully() {
            String refreshToken = "refresh-token";

            authenticationService.logout(refreshToken);

            verify(refreshTokenService).revokeToken(refreshToken);
        }

        @Test
        @DisplayName("Should logout from all devices")
        void shouldLogoutFromAllDevices() {
            when(userService.getUserEntity("test@example.com")).thenReturn(testUser);

            authenticationService.logoutAllDevices("test@example.com");

            verify(refreshTokenService).revokeAllUserTokens(testUser.getId());
        }
    }

    @Nested
    @DisplayName("Sign Up Tests")
    class SignUpTests {

        @Test
        @DisplayName("Should register new user and send verification email")
        void shouldRegisterNewUser() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "new@example.com", "password123", "New User");

            when(userService.createUnverifiedUser(request)).thenReturn(testUser);

            authenticationService.signUp(request);

            verify(userService).prepareUserForVerification(testUser);
            verify(authEmailService).sendVerificationEmail(testUser.getEmail());
        }
    }

    @Nested
    @DisplayName("Verify User Tests")
    class VerifyUserTests {

        @Test
        @DisplayName("Should verify user successfully")
        void shouldVerifyUserSuccessfully() {
            // Use the public constructor with code
            Verification verification = new Verification("ABCD1234");
            testUser.setVerification(verification);
            testUser.setIsVerified(false);
            VerificationRequest request = new VerificationRequest("test@example.com", "ABCD1234");

            when(rateLimitingService.canAttemptVerification(request.email())).thenReturn(true);
            when(userService.getUserEntity(request.email())).thenReturn(testUser);
            doNothing().when(verificationService).verifyCode(testUser, request.code());

            authenticationService.verifyUser(request);

            assertThat(testUser.getIsVerified()).isTrue();
            assertThat(testUser.getVerification()).isNull();
            verify(userRepository).save(testUser);
            verify(rateLimitingService).clearVerificationAttempts(request.email());
        }

        @Test
        @DisplayName("Should throw when rate limit exceeded")
        void shouldThrowWhenRateLimitExceeded() {
            VerificationRequest request = new VerificationRequest("test@example.com", "ABCD1234");

            when(rateLimitingService.canAttemptVerification(request.email())).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.verifyUser(request))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("exceeded");
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            VerificationRequest request = new VerificationRequest("nonexistent@example.com", "ABCD1234");

            when(rateLimitingService.canAttemptVerification(request.email())).thenReturn(true);
            when(userService.getUserEntity(request.email())).thenReturn(null);

            assertThatThrownBy(() -> authenticationService.verifyUser(request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(rateLimitingService).recordVerificationAttempt(request.email());
        }

        @Test
        @DisplayName("Should throw when no verification code exists")
        void shouldThrowWhenNoVerificationCode() {
            testUser.setVerification(null);
            VerificationRequest request = new VerificationRequest("test@example.com", "ABCD1234");

            when(rateLimitingService.canAttemptVerification(request.email())).thenReturn(true);
            when(userService.getUserEntity(request.email())).thenReturn(testUser);

            assertThatThrownBy(() -> authenticationService.verifyUser(request))
                    .isInstanceOf(VerificationCodeNotFoundException.class);

            verify(rateLimitingService).recordVerificationAttempt(request.email());
        }

        @Test
        @DisplayName("Should record attempt on verification failure")
        void shouldRecordAttemptOnVerificationFailure() {
            Verification verification = new Verification("ABCD1234");
            testUser.setVerification(verification);
            VerificationRequest request = new VerificationRequest("test@example.com", "WRONG123");

            when(rateLimitingService.canAttemptVerification(request.email())).thenReturn(true);
            when(userService.getUserEntity(request.email())).thenReturn(testUser);
            doThrow(new VerificationException("Invalid code")).when(verificationService)
                    .verifyCode(testUser, request.code());

            assertThatThrownBy(() -> authenticationService.verifyUser(request))
                    .isInstanceOf(VerificationException.class);

            verify(rateLimitingService).recordVerificationAttempt(request.email());
        }

        @Test
        @DisplayName("Should auto-verify SUPER_ADMIN")
        void shouldAutoVerifySuperAdmin() {
            testUser.setRole(superAdminRole);
            testUser.setIsVerified(false);
            VerificationRequest request = new VerificationRequest("admin@example.com", "ABCD1234");

            when(rateLimitingService.canAttemptVerification(request.email())).thenReturn(true);
            when(userService.getUserEntity(request.email())).thenReturn(testUser);

            authenticationService.verifyUser(request);

            assertThat(testUser.getIsVerified()).isTrue();
            verify(userRepository).save(testUser);
            verify(verificationService, never()).verifyCode(any(), anyString());
        }
    }

    @Nested
    @DisplayName("Resend Verification Tests")
    class ResendVerificationTests {

        @Test
        @DisplayName("Should resend verification email successfully")
        void shouldResendVerificationSuccessfully() {
            testUser.setIsVerified(false);
            String email = "test@example.com";

            when(rateLimitingService.canResendVerification(email)).thenReturn(true);
            when(userService.getUserEntity(email)).thenReturn(testUser);

            authenticationService.resendVerification(email);

            verify(userService).prepareUserForVerification(testUser);
            verify(authEmailService).sendVerificationEmail(email);
            verify(rateLimitingService).recordResendAttempt(email);
        }

        @Test
        @DisplayName("Should throw when rate limit exceeded")
        void shouldThrowWhenRateLimitExceeded() {
            when(rateLimitingService.canResendVerification("test@example.com")).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.resendVerification("test@example.com"))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("too many verification codes");
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            String email = "nonexistent@example.com";

            when(rateLimitingService.canResendVerification(email)).thenReturn(true);
            when(userService.getUserEntity(email)).thenReturn(null);

            assertThatThrownBy(() -> authenticationService.resendVerification(email))
                    .isInstanceOf(UserNotFoundException.class);

            verify(rateLimitingService).recordResendAttempt(email);
        }

        @Test
        @DisplayName("Should throw when user already verified")
        void shouldThrowWhenUserAlreadyVerified() {
            testUser.setIsVerified(true);
            String email = "test@example.com";

            when(rateLimitingService.canResendVerification(email)).thenReturn(true);
            when(userService.getUserEntity(email)).thenReturn(testUser);

            assertThatThrownBy(() -> authenticationService.resendVerification(email))
                    .isInstanceOf(UserAlreadyVerifiedException.class);

            verify(rateLimitingService).recordResendAttempt(email);
        }

        @Test
        @DisplayName("Should auto-verify and skip resend for SUPER_ADMIN")
        void shouldAutoVerifyAndSkipResendForSuperAdmin() {
            testUser.setRole(superAdminRole);
            testUser.setIsVerified(false);
            String email = "admin@example.com";

            when(rateLimitingService.canResendVerification(email)).thenReturn(true);
            when(userService.getUserEntity(email)).thenReturn(testUser);

            authenticationService.resendVerification(email);

            assertThat(testUser.getIsVerified()).isTrue();
            verify(userRepository).save(testUser);
            verify(authEmailService, never()).sendVerificationEmail(anyString());
            verify(rateLimitingService).recordResendAttempt(email);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null device info and IP in sign in")
        void shouldHandleNullDeviceInfoAndIp() {
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("test@example.com", "password123");
            Authentication authentication = mock(Authentication.class);
            RefreshToken refreshToken = new RefreshToken("refresh-token", testUser,
                    Instant.now().plus(7, ChronoUnit.DAYS));

            when(rateLimitingService.canAttemptLogin(request.email())).thenReturn(true);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(userDTOMapper.apply(testUser)).thenReturn(testUserDTO);
            when(jwtUtil.issueToken(anyString(), anyString())).thenReturn("access-token");
            when(jwtUtil.getAccessTokenExpirationMinutes()).thenReturn(15L);
            when(refreshTokenService.createRefreshToken(eq(testUser), isNull(), isNull()))
                    .thenReturn(refreshToken);

            AuthenticationResponse response = authenticationService.signIn(request, null, null);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should handle concurrent login attempts")
        void shouldHandleConcurrentLoginAttempts() {
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("test@example.com", "password123");

            // Simulate race condition where rate limit check passes but then fails
            when(rateLimitingService.canAttemptLogin(request.email()))
                    .thenReturn(true)
                    .thenReturn(false);
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authenticationService.signIn(request, null, null))
                    .isInstanceOf(BadCredentialsException.class);

            verify(rateLimitingService).recordFailedLoginAttempt(request.email());
        }

        @Test
        @DisplayName("Should preserve exception type for RateLimitExceededException in signIn")
        void shouldPreserveRateLimitExceptionType() {
            AuthenticationSigninRequest request = new AuthenticationSigninRequest("test@example.com", "password123");

            when(rateLimitingService.canAttemptLogin(request.email())).thenReturn(false);
            when(rateLimitingService.getRemainingLockoutMinutes(request.email())).thenReturn(5L);

            assertThatThrownBy(() -> authenticationService.signIn(request, null, null))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("5 minutes");

            // Should not record another failed attempt when already rate limited
            verify(rateLimitingService, never()).recordFailedLoginAttempt(anyString());
        }
    }
}
