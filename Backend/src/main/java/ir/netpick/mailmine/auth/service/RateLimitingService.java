package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.AuthConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class RateLimitingService {

    // Store rate limiting information for verification attempts
    private final Map<String, VerificationAttempts> verificationAttemptCounts = new ConcurrentHashMap<>();
    
    // Store rate limiting information for resend verification attempts
    private final Map<String, ResendAttempts> resendAttemptCounts = new ConcurrentHashMap<>();
    
    /**
     * Check if a user can attempt verification
     * @param email User's email
     * @return true if user can attempt verification, false otherwise
     */
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
     * @param email User's email
     */
    public void recordVerificationAttempt(String email) {
        verificationAttemptCounts.merge(email, 
            new VerificationAttempts(1, LocalDateTime.now()),
            (existing, newAttempt) -> new VerificationAttempts(existing.getAttempts() + 1, LocalDateTime.now()));
        
        log.debug("Recorded verification attempt for user: {}", email);
    }
    
    /**
     * Check if a user can resend verification
     * @param email User's email
     * @return true if user can resend verification, false otherwise
     */
    public boolean canResendVerification(String email) {
        ResendAttempts attempts = resendAttemptCounts.get(email);
        
        if (attempts == null) {
            return true;
        }
        
        // Limit to 3 resend attempts per hour
        if (attempts.getAttempts() >= 3) {
            // Check if the cooldown period has passed
            if (attempts.getLastAttempt().plusHours(1).isBefore(LocalDateTime.now())) {
                // Reset the attempts after cooldown
                resendAttemptCounts.remove(email);
                return true;
            }
            return false;
        }
        
        // Also enforce a minimum time between resends (30 seconds)
        if (attempts.getLastAttempt().plusSeconds(30).isAfter(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Record a resend verification attempt
     * @param email User's email
     */
    public void recordResendAttempt(String email) {
        resendAttemptCounts.merge(email,
            new ResendAttempts(1, LocalDateTime.now()),
            (existing, newAttempt) -> new ResendAttempts(existing.getAttempts() + 1, LocalDateTime.now()));
        
        log.debug("Recorded resend verification attempt for user: {}", email);
    }
    
    /**
     * Clear verification attempts after successful verification
     * @param email User's email
     */
    public void clearVerificationAttempts(String email) {
        verificationAttemptCounts.remove(email);
        log.debug("Cleared verification attempts for user: {}", email);
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
}