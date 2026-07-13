package ir.netpick.platform.gatekeeper.email;

import ir.netpick.platform.gatekeeper.AuthConstants;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.exception.VerificationException;
import ir.netpick.platform.mailmine.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Auth-specific email service that wraps the common EmailService
 * for verification-related emails.
 */
@Service("authEmailService")
@RequiredArgsConstructor
@Slf4j
public class AuthEmailService {

    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Send verification email to a user using their stored verification code
     */
    public void sendVerificationEmail(String email) {
        log.info("Sending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getVerification() == null) {
            throw new VerificationException("No verification code exists for user: " + email);
        }

        if (user.getVerification().isExpired()) {
            throw new VerificationException("Verification code has expired for user: " + email);
        }

        String code = user.getVerification().getCode();
        emailService.sendVerificationEmail(email, code, AuthConstants.VERIFICATION_CODE_EXPIRATION_TIME_MIN);

        // Update last sent timestamp
        user.getVerification().updateLastSent();
        userRepository.save(user);

        log.info("Verification email sent to: {}", email);
    }

    /**
     * Send password reset email to a user using their stored verification code
     */
    public void sendPasswordResetEmail(String email) {
        log.info("Sending password reset email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getVerification() == null) {
            throw new VerificationException("No verification code exists for user: " + email);
        }

        if (user.getVerification().isExpired()) {
            throw new VerificationException("Verification code has expired for user: " + email);
        }

        String code = user.getVerification().getCode();
        emailService.sendPasswordResetEmail(email, code, AuthConstants.VERIFICATION_CODE_EXPIRATION_TIME_MIN);

        // Update last sent timestamp
        user.getVerification().updateLastSent();
        userRepository.save(user);

        log.info("Password reset email sent to: {}", email);
    }
}









