package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.dto.*;
import ir.netpick.platform.gatekeeper.email.AuthEmailService;
import ir.netpick.platform.gatekeeper.exception.*;
import ir.netpick.platform.gatekeeper.jwt.JWTUtil;
import ir.netpick.platform.gatekeeper.mapper.UserDTOMapper;
import ir.netpick.platform.gatekeeper.model.RefreshToken;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import ir.netpick.platform.core.enums.RoleEnum;
import ir.netpick.platform.core.exception.RequestValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service class for handling authentication operations.
 * Provides methods for user sign-in, sign-up, verification, and related
 * operations.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserDTOMapper userDTOMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final VerificationService verificationService;
    private final AuthEmailService authEmailService;
    private final RateLimiting rateLimitingService;
    private final RefreshTokenService refreshTokenService;
    private final MfaService mfaService;
    private final DeviceSessionService deviceSessionService;
    private final SecurityEventService securityEventService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final IpPolicyService ipPolicyService;

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param request    the sign-in request containing user credentials
     * @param deviceInfo optional device information for refresh token
     * @param ipAddress  optional IP address for refresh token
     * @return an AuthenticationResponse with access and refresh tokens
     * @throws RateLimitExceededException  if the user has exceeded login attempts
     * @throws AccountNotVerifiedException if the account is not verified (except
     *                                     for SUPER_ADMIN users)
     */
    public AuthenticationResponse signIn(AuthenticationSigninRequest request, String deviceInfo, String ipAddress) {
        return signIn(request, deviceInfo, ipAddress, null);
    }

    /**
     * Authenticates a user with full security pipeline: IP policy, anomaly detection,
     * MFA challenge, device session tracking, and security event logging.
     */
    public AuthenticationResponse signIn(AuthenticationSigninRequest request, String deviceInfo,
                                         String ipAddress, String totpCode) {
        log.info("Attempting to sign in user with email: {}", request.email());

        // 1. IP Policy enforcement
        IpPolicyService.IpAccessResult ipResult = ipPolicyService.checkAccess(ipAddress);
        if (!ipResult.allowed()) {
            securityEventService.logEventSync(
                    ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.IP_BLOCKED,
                    null, request.email(), ipAddress, deviceInfo, null,
                    Map.of("reason", ipResult.reason()), 30, true);
            throw new RateLimitExceededException("Access denied from this IP address.");
        }

        // 2. Check login rate limiting
        if (!rateLimitingService.canAttemptLogin(request.email())) {
            long remainingMinutes = rateLimitingService.getRemainingLockoutMinutes(request.email());
            log.warn("Login rate limit exceeded for user: {}. Locked for {} more minutes.",
                    request.email(), remainingMinutes);
            securityEventService.logEventSync(
                    ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.LOGIN_LOCKED,
                    null, request.email(), ipAddress, deviceInfo, null,
                    Map.of("lockoutMinutes", remainingMinutes), 40, true);
            throw new RateLimitExceededException(
                    "Too many failed login attempts. " +
                            "Please try again in " + remainingMinutes + " minutes.");
        }

        try {
            Authentication authenticationResponse = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            User user = (User) authenticationResponse.getPrincipal();
            UserDTO userDTO = userDTOMapper.apply(user);

            // 3. Check if user is verified
            if (!userDTO.isVerified() && userDTO.role() != RoleEnum.SUPER_ADMIN) {
                log.warn("Account not verified for user: {}", request.email());
                throw new AccountNotVerifiedException(
                        "Your account is not verified. " +
                                "Please check your email for a verification code and verify your account.");
            }

            // 4. Anomaly detection - use null fingerprint (handled by DeviceSessionService)
            AnomalyDetectionService.AnomalyAnalysis anomaly =
                    anomalyDetectionService.analyzeLogin(request.email(), ipAddress, null, true);
            if (anomaly.blocked()) {
                rateLimitingService.recordFailedLoginAttempt(request.email());
                throw new RateLimitExceededException("Login blocked due to suspicious activity. Please try again later.");
            }

            // 5. MFA check
            if (mfaService.isMfaEnabled(user.getId())) {
                if (totpCode == null || totpCode.isBlank()) {
                    securityEventService.logEventSync(
                            ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.LOGIN_SUCCESS,
                            user.getId(), user.getEmail(), ipAddress, deviceInfo, deviceFingerprint,
                            Map.of("mfaRequired", true), 0, false);
                    throw new MfaRequiredException("MFA verification required. Please provide totpCode.", user.getEmail());
                }

                boolean mfaValid = mfaService.validateTotpCode(user, totpCode);
                if (!mfaValid) {
                    boolean backupUsed = mfaService.validateBackupCode(user, totpCode);
                    if (!backupUsed) {
                        securityEventService.logEventSync(
                                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.MFA_CODE_FAILED,
                                user.getId(), user.getEmail(), ipAddress, deviceInfo, deviceFingerprint,
                                Map.of(), 20, false);
                        throw new RateLimitExceededException("Invalid MFA code. Please try again.");
                    }
                }

                securityEventService.logEventSync(
                        ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.MFA_CODE_VERIFIED,
                        user.getId(), user.getEmail(), ipAddress, deviceInfo, deviceFingerprint,
                        Map.of(), 0, false);
            }

            // 6. Clear login attempts on successful login
            rateLimitingService.clearLoginAttempts(request.email());

            // 7. Generate tokens
            String accessToken = jwtUtil.issueToken(userDTO.email(), userDTO.role().toString());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, deviceInfo, ipAddress);

            // 8. Create device session
            jakarta.servlet.http.HttpServletRequest httpReq = getCurrentRequest();
            deviceSessionService.createSession(user, refreshToken, deviceInfo, ipAddress, httpReq);

            userService.updateLastSign(request.email());

            // 9. Log success
            securityEventService.logEventSync(
                    ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.LOGIN_SUCCESS,
                    user.getId(), user.getEmail(), ipAddress, deviceInfo, deviceFingerprint,
                    Map.of("riskScore", anomaly.riskScore()), 0, false);

            log.info("User successfully signed in: {}", request.email());

            return new AuthenticationResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtUtil.getAccessTokenExpirationMinutes() * 60
            );
        } catch (AccountNotVerifiedException | MfaRequiredException e) {
            throw e;
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            rateLimitingService.recordFailedLoginAttempt(request.email());
            securityEventService.logEventSync(
                    ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.LOGIN_FAILURE,
                    null, request.email(), ipAddress, deviceInfo, null,
                    Map.of("error", e.getClass().getSimpleName()), 10, false);
            log.error("Authentication failed for user: {}", request.email(), e);
            throw e;
        }
    }

    private jakarta.servlet.http.HttpServletRequest getCurrentRequest() {
        try {
            return ((jakarta.servlet.http.HttpServletRequest)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes().getRequest());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Refresh access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @param deviceInfo   optional device information for new refresh token
     * @param ipAddress    optional IP address for new refresh token
     * @return a new AuthenticationResponse with new access and refresh tokens
     */
    @Transactional
    public AuthenticationResponse refreshAccessToken(String refreshToken, String deviceInfo, String ipAddress) {
        log.info("Attempting to refresh access token");

        // Verify the refresh token
        RefreshToken token = refreshTokenService.verifyRefreshToken(refreshToken);
        User user = token.getUser();

        // Check if user is still active (not deleted)
        if (Boolean.TRUE.equals(user.getDeleted())) {
            log.warn("Refresh token used for deleted user: {}", user.getEmail());
            refreshTokenService.revokeToken(refreshToken);
            throw new AccountNotVerifiedException("Account has been deactivated");
        }

        // Generate new access token
        String accessToken = jwtUtil.issueToken(user.getEmail(), user.getRole().getName().toString());

        // Rotate refresh token (revoke old, create new)
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                refreshToken, user, deviceInfo, ipAddress);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return new AuthenticationResponse(
                accessToken,
                newRefreshToken.getToken(),
                jwtUtil.getAccessTokenExpirationMinutes() * 60);
    }

    /**
     * Logout user by revoking their refresh token.
     *
     * @param refreshToken the refresh token to revoke
     */
    public void logout(String refreshToken) {
        log.info("Logging out user");
        refreshTokenService.revokeToken(refreshToken);
        log.info("User logged out successfully");
    }

    /**
     * Logout user from all devices by revoking all refresh tokens.
     *
     * @param email the email of the user to logout from all devices
     */
    public void logoutAllDevices(String email) {
        log.info("Logging out user from all devices: {}", email);
        User user = userService.getUserEntity(email);
        refreshTokenService.revokeAllUserTokens(user.getId());
        log.info("User logged out from all devices: {}", email);
    }

    /**
     * Registers a new user and sends a verification email.
     *
     * @param request the sign-up request containing user details
     * @throws RequestValidationException if the registration request is invalid
     */
    public void signUp(AuthenticationSignupRequest request) {
        log.info("Attempting to register new user with email: {}", request.email());

        try {
            User user = userService.createUnverifiedUser(request);

            // Prepare user for verification and send email asynchronously
            userService.prepareUserForVerification(user);
            // Send email asynchronously - this won't block the response
            authEmailService.sendVerificationEmail(user.getEmail());
            log.info("Verification email queued for sending to: {}", user.getEmail());

            log.info("User registered successfully: {}", request.email());
        } catch (Exception e) {
            log.error("User registration failed for email: {}", request.email(), e);
            throw e;
        }
    }

    /**
     * Verifies a user using a verification code.
     *
     * @param request the verification request containing user email and
     *                verification code
     * @throws RateLimitExceededException        if the user has exceeded
     *                                           verification attempts
     * @throws UserNotFoundException             if no user exists with the given
     *                                           email
     * @throws VerificationCodeNotFoundException if no verification code exists for
     *                                           the user
     */
    public void verifyUser(VerificationRequest request) {
        log.info("Attempting to verify user with email: {}", request.email());

        try {
            // Check rate limiting
            if (!rateLimitingService.canAttemptVerification(request.email())) {
                log.info("Rate limit exceeded for verification attempts: {}", request.email());
                throw new RateLimitExceededException(
                        "You've exceeded the maximum number of verification attempts. " +
                                "Please wait 10 minutes before trying again.");
            }

            User user = userService.getUserEntity(request.email());

            if (user == null) {
                log.info("User not found for verification: {}", request.email());
                rateLimitingService.recordVerificationAttempt(request.email());
                throw new UserNotFoundException(
                        "No account found with this email address. " +
                                "Please check the email or register for a new account.");
            }

            // Skip verification for SUPER_ADMIN users
            if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                log.info("Skipping verification for SUPER_ADMIN user: {}", request.email());
                if (!user.getIsVerified()) {
                    user.setIsVerified(true);
                    userRepository.save(user);
                }
                return;
            }

            if (user.getVerification() == null) {
                log.info("No verification code found for user: {}", request.email());
                rateLimitingService.recordVerificationAttempt(request.email());
                throw new VerificationCodeNotFoundException(
                        "No verification code found for your account. " +
                                "Please request a new verification code.");
            }

            try {
                verificationService.verifyCode(user, request.code());
                rateLimitingService.clearVerificationAttempts(request.email());
            } catch (Exception e) {
                rateLimitingService.recordVerificationAttempt(request.email());
                throw e;
            }

            user.setIsVerified(true);
            user.setVerification(null);
            userRepository.save(user);
            log.info("User successfully verified: {}", request.email());
        } catch (RateLimitExceededException | UserNotFoundException | VerificationCodeNotFoundException e) {
            log.info("User verification failed due to expected condition for email: {}", request.email());
            throw e;
        } catch (Exception e) {
            log.error("User verification failed unexpectedly for email: {}", request.email(), e);
            throw e;
        }
    }

    /**
     * Resends a verification email to a user.
     *
     * @param email the email address of the user to resend verification to
     * @throws RateLimitExceededException   if the user has exceeded resend attempts
     * @throws UserNotFoundException        if no user exists with the given email
     * @throws UserAlreadyVerifiedException if the user is already verified
     */
    public void resendVerification(String email) {
        log.info("Attempting to resend verification for user: {}", email);

        try {
            // Check rate limiting
            if (!rateLimitingService.canResendVerification(email)) {
                log.info("Rate limit exceeded for resend verification attempts: {}", email);
                throw new RateLimitExceededException(
                        "You've requested too many verification codes. " +
                                "Please wait an hour before requesting another one.");
            }

            User user = userService.getUserEntity(email);

            if (user == null) {
                log.info("User not found for resend verification: {}", email);
                rateLimitingService.recordResendAttempt(email);
                throw new UserNotFoundException(
                        "No account found with this email address. " +
                                "Please check the email or register for a new account.");
            }

            // Skip verification for SUPER_ADMIN users
            if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                log.info("Skipping verification resend for SUPER_ADMIN user: {}", email);
                if (!user.getIsVerified()) {
                    user.setIsVerified(true);
                    userRepository.save(user);
                }
                rateLimitingService.recordResendAttempt(email);
                return;
            }

            if (Boolean.TRUE.equals(user.getIsVerified())) {
                log.info("User already verified: {}", email);
                rateLimitingService.recordResendAttempt(email);
                throw new UserAlreadyVerifiedException(
                        "Your account is already verified. " +
                                "You can sign in directly without verification.");
            }

            // Update verification with new code
            userService.prepareUserForVerification(user);
            // Send email asynchronously - this won't block the response
            authEmailService.sendVerificationEmail(email);
            rateLimitingService.recordResendAttempt(email);
            log.info("Verification email resent to: {}", email);
        } catch (RateLimitExceededException | UserNotFoundException | UserAlreadyVerifiedException e) {
            log.info("Resend verification failed due to expected condition for email: {}", email);
            throw e;
        } catch (Exception e) {
            log.error("Resend verification failed unexpectedly for email: {}", email, e);
            throw e;
        }
    }

    /**
     * Request a password reset code to be sent to the user's email.
     * Always returns successfully to prevent user enumeration.
     *
     * @param request the password reset request containing user email
     */
    public void requestPasswordReset(PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.email());

        try {
            // Check rate limiting - apply to all emails to prevent enumeration
            if (!rateLimitingService.canResendVerification(request.email())) {
                log.info("Rate limit exceeded for password reset attempts: {}", request.email());
                // Still throw to prevent enumeration attacks
                throw new RateLimitExceededException(
                        "You've requested too many password reset codes. " +
                                "Please wait an hour before requesting another one.");
            }

            User user = userService.getUserEntity(request.email());

            // Check if user exists and is not SUPER_ADMIN
            if (user == null || user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                // Always record attempt to prevent enumeration
                rateLimitingService.recordResendAttempt(request.email());
                log.info("Password reset processed (user may not exist) for: {}", request.email());
                return;
            }

            // Update verification with new code for password reset
            userService.prepareUserForVerification(user);
            // Send email asynchronously - this won't block the response
            authEmailService.sendPasswordResetEmail(request.email());
            rateLimitingService.recordResendAttempt(request.email());
            log.info("Password reset email sent to: {}", request.email());
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password reset request failed unexpectedly for email: {}", request.email(), e);
            throw e;
        }
    }

    /**
     * Verify password reset code.
     * Always returns same response to prevent user enumeration.
     *
     * @param request the password reset verify request containing email and code
     */
    public void verifyPasswordResetCode(PasswordResetVerifyRequest request) {
        log.info("Verifying password reset code for email: {}", request.email());

        try {
            // Check rate limiting
            if (!rateLimitingService.canAttemptVerification(request.email())) {
                log.info("Rate limit exceeded for password reset verification attempts: {}", request.email());
                throw new RateLimitExceededException(
                        "You've exceeded the maximum number of verification attempts. " +
                                "Please wait 10 minutes before trying again.");
            }

            User user = userService.getUserEntity(request.email());

            // Check if user exists and is not SUPER_ADMIN
            if (user == null || user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                // Always record attempt and throw generic error to prevent enumeration
                rateLimitingService.recordVerificationAttempt(request.email());
                log.info("Password reset verification processed (user may not exist) for: {}", request.email());
                throw new VerificationCodeNotFoundException(
                        "If an account exists with this email, a verification code has been sent. " +
                                "Please request a new password reset if this code is invalid.");
            }

            if (user.getVerification() == null) {
                log.info("No verification pending for password reset: {}", request.email());
                rateLimitingService.recordVerificationAttempt(request.email());
                throw new VerificationCodeNotFoundException(
                        "Invalid or expired verification code. Please request a new password reset.");
            }

            try {
                verificationService.verifyCode(user, request.code());
                rateLimitingService.clearVerificationAttempts(request.email());
            } catch (Exception e) {
                rateLimitingService.recordVerificationAttempt(request.email());
                throw e;
            }

            log.info("Password reset code verified for: {}", request.email());
        } catch (RateLimitExceededException | VerificationCodeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password reset verification failed unexpectedly for email: {}", request.email(), e);
            throw e;
        }
    }

    /**
     * Confirm and complete password reset.
     *
     * @param request the password reset confirm request containing email, code, and new password
     * @throws RateLimitExceededException        if the user has exceeded verification attempts
     * @throws UserNotFoundException             if no user exists with the given email
     * @throws VerificationCodeNotFoundException if no verification code exists for the user
     */
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        log.info("Confirming password reset for email: {}", request.email());

        try {
            User user = userService.getUserEntity(request.email());

            if (user == null || user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                log.info("Password reset confirmation processed (user may not exist) for: {}", request.email());
                return;
            }

            if (user.getVerification() == null) {
                log.info("No verification pending for password reset confirmation: {}", request.email());
                return;
            }

// Verify the code one more time
            verificationService.verifyCode(user, request.code());

            // Update password
            userService.updatePassword(user, request.password());

            // Clear verification
            user.setVerification(null);
            userRepository.save(user);

            log.info("Password reset completed successfully for: {}", request.email());
        } catch (Exception e) {
            log.error("Password reset confirmation failed for email: {}", request.email(), e);
            // Don't throw - return silently to prevent enumeration
        }
    }
}








