package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.dto.AuthenticationResponse;
import ir.netpick.platform.gatekeeper.dto.AuthenticationSigninRequest;
import ir.netpick.platform.gatekeeper.dto.AuthenticationSignupRequest;
import ir.netpick.platform.gatekeeper.dto.PasswordResetConfirmRequest;
import ir.netpick.platform.gatekeeper.dto.PasswordResetRequest;
import ir.netpick.platform.gatekeeper.dto.PasswordResetVerifyRequest;
import ir.netpick.platform.gatekeeper.dto.UserDTO;
import ir.netpick.platform.gatekeeper.dto.VerificationRequest;
import ir.netpick.platform.gatekeeper.email.AuthEmailService;
import ir.netpick.platform.gatekeeper.exception.AccountNotVerifiedException;
import ir.netpick.platform.gatekeeper.exception.RateLimitExceededException;
import ir.netpick.platform.gatekeeper.exception.UserAlreadyVerifiedException;
import ir.netpick.platform.gatekeeper.exception.UserNotFoundException;
import ir.netpick.platform.gatekeeper.exception.VerificationCodeNotFoundException;
import ir.netpick.platform.gatekeeper.model.RefreshToken;
import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.gatekeeper.jwt.JWTUtil;
import ir.netpick.platform.gatekeeper.mapper.UserDTOMapper;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import ir.netpick.platform.core.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("Attempting to sign in user with email: {}", request.email());

        // Check login rate limiting
        if (!rateLimitingService.canAttemptLogin(request.email())) {
            long remainingMinutes = rateLimitingService.getRemainingLockoutMinutes(request.email());
            log.warn("Login rate limit exceeded for user: {}. Locked for {} more minutes.",
                    request.email(), remainingMinutes);
            throw new RateLimitExceededException(
                    "Too many failed login attempts. " +
                            "Please try again in " + remainingMinutes + " minutes.");
        }

        try {
            Authentication authenticationResponse = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            User user = (User) authenticationResponse.getPrincipal();
            UserDTO userDTO = userDTOMapper.apply(user);

            // Check if user is verified (except for SUPER_ADMIN users)
            if (!userDTO.isVerified() && userDTO.role() != RoleEnum.SUPER_ADMIN) {
                log.warn("Account not verified for user: {}", request.email());
                throw new AccountNotVerifiedException(
                        "Your account is not verified. " +
                                "Please check your email for a verification code and verify your account.");
            }

            // Clear login attempts on successful login
            rateLimitingService.clearLoginAttempts(request.email());

            // Generate access token
            String accessToken = jwtUtil.issueToken(userDTO.email(), userDTO.role().toString());

            // Generate refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, deviceInfo, ipAddress);

            userService.updateLastSign(request.email());
            log.info("User successfully signed in: {}", request.email());

            return new AuthenticationResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtUtil.getAccessTokenExpirationMinutes() * 60 // Convert to seconds
            );
        } catch (AccountNotVerifiedException e) {
            log.info("Sign in failed due to unverified account: {}", request.email());
            throw e;
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            // Record failed login attempt
            rateLimitingService.recordFailedLoginAttempt(request.email());
            log.error("Authentication failed for user: {}", request.email(), e);
            throw e;
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
     *
     * @param request the password reset request containing user email
     * @throws RateLimitExceededException if the user has exceeded resend attempts
     * @throws UserNotFoundException       if no user exists with the given email
     */
    public void requestPasswordReset(PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.email());

        try {
            // Check rate limiting
            if (!rateLimitingService.canResendVerification(request.email())) {
                log.info("Rate limit exceeded for password reset attempts: {}", request.email());
                throw new RateLimitExceededException(
                        "You've requested too many password reset codes. " +
                                "Please wait an hour before requesting another one.");
            }

            User user = userService.getUserEntity(request.email());

            if (user == null) {
                log.info("User not found for password reset: {}", request.email());
                rateLimitingService.recordResendAttempt(request.email());
                throw new UserNotFoundException(
                        "No account found with this email address.");
            }

            // Skip for SUPER_ADMIN users - they don't need password reset via email
            if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                log.info("Skipping password reset for SUPER_ADMIN user: {}", request.email());
                rateLimitingService.recordResendAttempt(request.email());
                return;
            }

            // Update verification with new code for password reset
            userService.prepareUserForVerification(user);
            // Send email asynchronously - this won't block the response
            authEmailService.sendPasswordResetEmail(request.email());
            rateLimitingService.recordResendAttempt(request.email());
            log.info("Password reset email sent to: {}", request.email());
        } catch (RateLimitExceededException | UserNotFoundException e) {
            log.info("Password reset request failed due to expected condition for email: {}", request.email());
            throw e;
        } catch (Exception e) {
            log.error("Password reset request failed unexpectedly for email: {}", request.email(), e);
            throw e;
        }
    }

    /**
     * Verify password reset code.
     *
     * @param request the password reset verify request containing email and code
     * @throws RateLimitExceededException        if the user has exceeded verification attempts
     * @throws UserNotFoundException             if no user exists with the given email
     * @throws VerificationCodeNotFoundException if no verification code exists for the user
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

            if (user == null) {
                log.info("User not found for password reset verification: {}", request.email());
                rateLimitingService.recordVerificationAttempt(request.email());
                throw new UserNotFoundException(
                        "No account found with this email address.");
            }

            // Skip verification for SUPER_ADMIN users
            if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                log.info("Skipping password reset verification for SUPER_ADMIN user: {}", request.email());
                return;
            }

            if (user.getVerification() == null) {
                log.info("No verification pending for password reset: {}", request.email());
                rateLimitingService.recordVerificationAttempt(request.email());
                throw new VerificationCodeNotFoundException(
                        "No password reset request found for this account. " +
                                "Please request a new password reset.");
            }

            try {
                verificationService.verifyCode(user, request.code());
                rateLimitingService.clearVerificationAttempts(request.email());
            } catch (Exception e) {
                rateLimitingService.recordVerificationAttempt(request.email());
                throw e;
            }

            log.info("Password reset code verified for: {}", request.email());
        } catch (RateLimitExceededException | UserNotFoundException | VerificationCodeNotFoundException e) {
            log.info("Password reset verification failed due to expected condition for email: {}", request.email());
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

            if (user == null) {
                log.info("User not found for password reset confirmation: {}", request.email());
                throw new UserNotFoundException(
                        "No account found with this email address.");
            }

            // Skip for SUPER_ADMIN users
            if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
                log.info("Skipping password reset confirmation for SUPER_ADMIN user: {}", request.email());
                return;
            }

            if (user.getVerification() == null) {
                log.info("No verification pending for password reset confirmation: {}", request.email());
                throw new VerificationCodeNotFoundException(
                        "No password reset request found for this account.");
            }

            // Verify the code one more time
            verificationService.verifyCode(user, request.code());

            // Update password
            userService.updatePassword(user, request.password());

            // Clear verification
            user.setVerification(null);
            userRepository.save(user);

            log.info("Password reset completed successfully for: {}", request.email());
        } catch (RateLimitExceededException | UserNotFoundException | VerificationCodeNotFoundException e) {
            log.info("Password reset confirmation failed due to expected condition for email: {}", request.email());
            throw e;
        } catch (Exception e) {
            log.error("Password reset confirmation failed unexpectedly for email: {}", request.email(), e);
            throw e;
        }
    }

}








