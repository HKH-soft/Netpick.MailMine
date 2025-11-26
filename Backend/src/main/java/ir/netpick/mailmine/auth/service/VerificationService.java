package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.AuthConstants;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.model.Verification;
import static ir.netpick.mailmine.auth.AuthConstants.*;

import ir.netpick.mailmine.common.exception.VerificationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class VerificationService {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Verifies a code against the user's verification record
     */
    public void verifyCode(Verification verification, String code) {
        if (verification == null)
            throw new VerificationException("No verification pending");

        if (verification.isExpired())
            throw new VerificationException("Verification expired");

        if (verification.hasReachedMaxAttempts())
            throw new VerificationException("Too many attempts");

        if (!verification.matches(code)) {
            verification.incrementAttempts();
            throw new VerificationException("Invalid code");
        }

        // otherwise success
    }

    /**
     * Creates a new verification record
     */
    public Verification createVerification(){
        return new Verification(generateVerificationCode());
    }

    /**
     * Updates an existing verification with a new code
     */
    public void updateVerification(Verification verification) {
        verification.updateCode(generateVerificationCode());
    }

    // verification code generation
    public String generateVerificationCode() {
        return randomLetters(VERIFICATION_CODE_LETTER_COUNT) + randomDigits(VERIFICATION_CODE_DIGIT_COUNT);
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