package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.exception.UserAlreadyVerifiedException;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.model.Verification;
import ir.netpick.mailmine.auth.repository.UserRepository;
import static ir.netpick.mailmine.auth.AuthConstants.*;

import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.common.exception.VerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Service class for handling user verification operations.
 * Provides methods for creating, updating, and verifying user verification
 * codes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final UserRepository userRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Verifies a code against the user's verification record.
     *
     * @param user the user whose verification to check
     * @param code the verification code to check
     * @throws VerificationException if the verification fails for any reason
     */
    public void verifyCode(User user, String code) {
        log.debug("Attempting to verify code for user: {}", user.getEmail());

        Verification verification = user.getVerification();

        if (verification == null) {
            log.warn("No verification pending for user: {}", user.getEmail());
            throw new VerificationException(
                    "No verification is currently pending for your account. " +
                            "Please request a new verification code.");
        }

        if (verification.isExpired()) {
            log.warn("Verification code expired at: {} for user: {}",
                    verification.getVerificationExpiresAt(), user.getEmail());
            throw new VerificationException(
                    "Your verification code has expired. " +
                            "Please request a new verification code.");
        }

        if (verification.hasReachedMaxAttempts()) {
            log.warn("Too many verification attempts. Attempts: {} for user: {}",
                    verification.getAttempts(), user.getEmail());
            throw new VerificationException(
                    "You've exceeded the maximum number of verification attempts. " +
                            "Please wait 10 minutes or request a new code.");
        }

        if (!verification.matches(code)) {
            verification.incrementAttempts();
            // Persist the attempt count to database
            userRepository.save(user);
            log.warn("Invalid verification code provided. Attempt #{} for user: {}",
                    verification.getAttempts(), user.getEmail());
            throw new VerificationException(
                    "The verification code you entered is invalid. " +
                            "Please check the code and try again.");
        }

        log.info("Verification code successfully validated for user: {}", user.getEmail());
        // otherwise success
    }

    /**
     * Creates a new verification record with a randomly generated code.
     *
     * @return a new Verification record
     */
    public Verification createVerification() {
        log.debug("Creating new verification record");
        Verification verification = new Verification(generateVerificationCode());
        log.debug("New verification record created");
        return verification;
    }

    /**
     * Updates an existing verification with a new randomly generated code.
     *
     * @param verification the verification record to update
     */
    public void updateVerification(Verification verification) {
        log.debug("Updating verification record");
        verification.updateCode(generateVerificationCode());
        log.debug("Verification code updated successfully");
    }

    /**
     * Prepares a user for verification by creating or updating verification code.
     * SUPER_ADMIN users are automatically verified and do not receive verification
     * codes.
     *
     * @param user The user to prepare for verification
     * @return The verification code that was created/updated, or null for
     *         SUPER_ADMIN users
     * @throws UserAlreadyVerifiedException if the user is already verified
     */
    public String prepareUserForVerification(User user) {
        log.info("Preparing user for verification: {}", user.getEmail());

        // If user already verified, no need to send verification
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            log.warn("User already verified: {}", user.getEmail());
            throw new UserAlreadyVerifiedException(
                    "Your account is already verified. " +
                            "You can sign in directly without verification.");
        }

        // Skip verification for SUPER_ADMIN users
        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.info("Skipping verification for SUPER_ADMIN user: {}", user.getEmail());
            user.setIsVerified(true);
            userRepository.save(user);
            return null; // No verification code needed
        }

        String verificationCode;
        // Create or update verification code
        if (user.getVerification() == null) {
            log.debug("Creating new verification for user: {}", user.getEmail());
            Verification verification = createVerification();
            user.setVerification(verification);
            userRepository.save(user);
            verificationCode = verification.getCode();
        } else {
            log.debug("Updating existing verification for user: {}", user.getEmail());
            // Update with new code
            updateVerification(user.getVerification());
            verificationCode = user.getVerification().getCode();

            // Save the updated user
            userRepository.save(user);
        }

        log.info("User verification prepared successfully: {}", user.getEmail());
        return verificationCode;
    }

    /**
     * Generates a random verification code consisting of letters and digits.
     *
     * @return a randomly generated verification code
     */
    // verification code generation
    public String generateVerificationCode() {
        String letters = randomLetters(VERIFICATION_CODE_LETTER_COUNT);
        String digits = randomDigits(VERIFICATION_CODE_DIGIT_COUNT);
        String code = letters + digits;
        log.trace("Verification code generated"); // Code intentionally not logged for security
        return code;
    }

    private String randomLetters(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(VERIFICATION_LETTERS.charAt(secureRandom.nextInt(VERIFICATION_LETTERS.length())));
        }
        return sb.toString();
    }

    private String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(VERIFICATION_DIGITS.charAt(secureRandom.nextInt(VERIFICATION_DIGITS.length())));
        }
        return sb.toString();
    }
}