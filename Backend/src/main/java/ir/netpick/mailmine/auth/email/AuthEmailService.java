package ir.netpick.mailmine.auth.email;

import ir.netpick.mailmine.auth.AuthConstants;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.common.exception.VerificationException;
import ir.netpick.mailmine.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Auth-specific email service that wraps the common EmailService
 * for verification-related emails.
 */
@Service("authEmailService")
@RequiredArgsConstructor
@Log4j2
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
}
