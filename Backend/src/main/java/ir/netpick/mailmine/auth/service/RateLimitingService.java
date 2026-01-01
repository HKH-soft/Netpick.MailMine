package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.AuthConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiting service for single-instance deployments
 * For distributed deployments, use RedisRateLimitingService instead
 * Controlled by property: rate-limiting.use-redis=false (default)
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "rate-limiting.use-redis", havingValue = "false", matchIfMissing = true)
public class RateLimitingService implements RateLimiting {

    // Resend rate limiting constants
    private static final int RESEND_MAX_PER_HOUR = 3;
    private static final int RESEND_MIN_SECONDS = 30;

    // Store rate limiting information for verification attempts
    private final Map<String, VerificationAttempts> verificationAttemptCounts = new ConcurrentHashMap<>();

    // Store rate limiting information for resend verification attempts
    private final Map<String, ResendAttempts> resendAttemptCounts = new ConcurrentHashMap<>();

    // Store rate limiting information for login attempts
    private final Map<String, LoginAttempts> loginAttemptCounts = new ConcurrentHashMap<>();

    // Login rate limiting constants
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_LOCKOUT_MINUTES = 15;

    /**
     * Check if a user can attempt login
     * 
     * @param email User's email
     * @return true if user can attempt login, false otherwise
     */
    @Override
    public boolean canAttemptLogin(String email) {
        LoginAttempts attempts = loginAttemptCounts.get(email);

        if (attempts == null) {
            return true;
        }

        // Check if the user has exceeded the maximum attempts
        if (attempts.getAttempts() >= MAX_LOGIN_ATTEMPTS) {
            // Check if the lockout period has passed
            if (attempts.getLastAttempt().plusMinutes(LOGIN_LOCKOUT_MINUTES).isBefore(LocalDateTime.now())) {
                // Reset the attempts after lockout
                loginAttemptCounts.remove(email);
                return true;
            }
            return false;
        }

        return true;
    }

    /**
     * Record a failed login attempt
     * 
     * @param email User's email
     */
    @Override
    public void recordFailedLoginAttempt(String email) {
        loginAttemptCounts.merge(email,
                new LoginAttempts(1, LocalDateTime.now()),
                (existing, newAttempt) -> new LoginAttempts(existing.getAttempts() + 1, LocalDateTime.now()));

        LoginAttempts attempts = loginAttemptCounts.get(email);
        log.warn("Failed login attempt #{} for user: {}", attempts.getAttempts(), email);
    }

    /**
     * Clear login attempts after successful login
     * 
     * @param email User's email
     */
    @Override
    public void clearLoginAttempts(String email) {
        loginAttemptCounts.remove(email);
        log.debug("Cleared login attempts for user: {}", email);
    }

    /**
     * Get remaining lockout time in minutes
     * 
     * @param email User's email
     * @return remaining minutes, or 0 if not locked out
     */
    @Override
    public long getRemainingLockoutMinutes(String email) {
        LoginAttempts attempts = loginAttemptCounts.get(email);
        if (attempts == null || attempts.getAttempts() < MAX_LOGIN_ATTEMPTS) {
            return 0;
        }

        LocalDateTime unlockTime = attempts.getLastAttempt().plusMinutes(LOGIN_LOCKOUT_MINUTES);
        if (unlockTime.isBefore(LocalDateTime.now())) {
            return 0;
        }

        return java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes() + 1;
    }

    /**
     * Check if a user can attempt verification
     * 
     * @param email User's email
     * @return true if user can attempt verification, false otherwise
     */
    @Override
    public boolean canAttemptVerification(String email) {
        VerificationAttempts attempts = verificationAttemptCounts.get(email);

        if (attempts == null) {
            return true;
        }

        // Check if the user has exceeded the maximum attempts
        if (attempts.getAttempts() >= AuthConstants.VERIFICATION_MAX_ATTEMPTS) {
            // Check if the cooldown period has passed
            if (attempts.getLastAttempt().plusMinutes(10).isBefore(LocalDateTime.now())) {
                // Reset the attempts after cooldown
                verificationAttemptCounts.remove(email);
                return true;
            }
            return false;
        }

        return true;
    }

    /**
     * Record a verification attempt
     * 
     * @param email User's email
     */
    @Override
    public void recordVerificationAttempt(String email) {
        verificationAttemptCounts.merge(email,
                new VerificationAttempts(1, LocalDateTime.now()),
                (existing, newAttempt) -> new VerificationAttempts(existing.getAttempts() + 1, LocalDateTime.now()));

        log.debug("Recorded verification attempt for user: {}", email);
    }

    /**
     * Check if a user can resend verification
     * 
     * @param email User's email
     * @return true if user can resend verification, false otherwise
     */
    @Override
    public boolean canResendVerification(String email) {
        ResendAttempts attempts = resendAttemptCounts.get(email);

        if (attempts == null) {
            return true;
        }

        // Limit to 3 resend attempts per hour
        if (attempts.getAttempts() >= RESEND_MAX_PER_HOUR) {
            // Check if the cooldown period has passed
            if (attempts.getLastAttempt().plusHours(1).isBefore(LocalDateTime.now())) {
                // Reset the attempts after cooldown
                resendAttemptCounts.remove(email);
                return true;
            }
            return false;
        }

        // Also enforce a minimum time between resends (30 seconds)
        if (attempts.getLastAttempt().plusSeconds(RESEND_MIN_SECONDS).isAfter(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    public int getResendMinSeconds() {
        return RESEND_MIN_SECONDS;
    }

    public int getResendMaxPerHour() {
        return RESEND_MAX_PER_HOUR;
    }

    /**
     * Record a resend verification attempt
     * 
     * @param email User's email
     */
    @Override
    public void recordResendAttempt(String email) {
        resendAttemptCounts.merge(email,
                new ResendAttempts(1, LocalDateTime.now()),
                (existing, newAttempt) -> new ResendAttempts(existing.getAttempts() + 1, LocalDateTime.now()));

        log.debug("Recorded resend verification attempt for user: {}", email);
    }

    /**
     * Clear verification attempts after successful verification
     * 
     * @param email User's email
     */
    @Override
    public void clearVerificationAttempts(String email) {
        verificationAttemptCounts.remove(email);
        log.debug("Cleared verification attempts for user: {}", email);
    }

    /**
     * Clear resend attempts (useful for testing)
     * 
     * @param email User's email
     */
    @Override
    public void clearResendAttempts(String email) {
        resendAttemptCounts.remove(email);
        log.debug("Cleared resend attempts for user: {}", email);
    }

    /**
     * Inner class to track verification attempts
     */
    private static class VerificationAttempts {
        private final int attempts;
        private final LocalDateTime lastAttempt;

        public VerificationAttempts(int attempts, LocalDateTime lastAttempt) {
            this.attempts = attempts;
            this.lastAttempt = lastAttempt;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }
    }

    /**
     * Inner class to track resend attempts
     */
    private static class ResendAttempts {
        private final int attempts;
        private final LocalDateTime lastAttempt;

        public ResendAttempts(int attempts, LocalDateTime lastAttempt) {
            this.attempts = attempts;
            this.lastAttempt = lastAttempt;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }
    }

    /**
     * Inner class to track login attempts
     */
    private static class LoginAttempts {
        private final int attempts;
        private final LocalDateTime lastAttempt;

        public LoginAttempts(int attempts, LocalDateTime lastAttempt) {
            this.attempts = attempts;
            this.lastAttempt = lastAttempt;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }
    }
}