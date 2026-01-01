package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiting service for distributed deployments
 * Uses Redis for storing rate limit information across multiple instances
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rate-limiting.use-redis", havingValue = "true", matchIfMissing = false)
public class RedisRateLimitingService implements RateLimiting {

    private final RedisTemplate<String, Object> redisTemplate;

    // Resend rate limiting constants
    private static final int RESEND_MAX_PER_HOUR = 3;
    private static final int RESEND_MIN_SECONDS = 30;

    // Login rate limiting constants
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_LOCKOUT_MINUTES = 15;

    // Redis key prefixes
    private static final String LOGIN_ATTEMPTS_PREFIX = "ratelimit:login:";
    private static final String VERIFICATION_ATTEMPTS_PREFIX = "ratelimit:verification:";
    private static final String RESEND_ATTEMPTS_PREFIX = "ratelimit:resend:";
    private static final String RESEND_LAST_TIME_PREFIX = "ratelimit:resend:lasttime:";

    /**
     * Check if a user can attempt login
     */
    @Override
    public boolean canAttemptLogin(String email) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            return true;
        }

        return attempts < MAX_LOGIN_ATTEMPTS;
    }

    /**
     * Record a failed login attempt
     */
    @Override
    public void recordFailedLoginAttempt(String email) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == 1) {
            // Set expiration on first attempt
            redisTemplate.expire(key, Duration.ofMinutes(LOGIN_LOCKOUT_MINUTES));
        }

        log.warn("Failed login attempt #{} for user: {}", attempts, email);
    }

    /**
     * Clear login attempts after successful login
     */
    @Override
    public void clearLoginAttempts(String email) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Cleared login attempts for user: {}", email);
    }

    /**
     * Get remaining lockout time in minutes
     */
    @Override
    public long getRemainingLockoutMinutes(String email) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);

        if (attempts == null || attempts < MAX_LOGIN_ATTEMPTS) {
            return 0;
        }

        Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * Check if a user can attempt verification
     */
    @Override
    public boolean canAttemptVerification(String email) {
        String key = VERIFICATION_ATTEMPTS_PREFIX + email;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            return true;
        }

        return attempts < AuthConstants.VERIFICATION_MAX_ATTEMPTS;
    }

    /**
     * Record a verification attempt
     */
    @Override
    public void recordVerificationAttempt(String email) {
        String key = VERIFICATION_ATTEMPTS_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == 1) {
            // Set expiration on first attempt (10 minutes cooldown)
            redisTemplate.expire(key, Duration.ofMinutes(10));
        }

        log.debug("Recorded verification attempt for user: {}", email);
    }

    /**
     * Check if a user can resend verification
     */
    @Override
    public boolean canResendVerification(String email) {
        String attemptsKey = RESEND_ATTEMPTS_PREFIX + email;
        String lastTimeKey = RESEND_LAST_TIME_PREFIX + email;

        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);
        Long lastTime = (Long) redisTemplate.opsForValue().get(lastTimeKey);

        // Check if user has exceeded max resend attempts per hour
        if (attempts != null && attempts >= RESEND_MAX_PER_HOUR) {
            return false;
        }

        // Check minimum time between resends
        if (lastTime != null) {
            long secondsSinceLastResend = (System.currentTimeMillis() - lastTime) / 1000;
            if (secondsSinceLastResend < RESEND_MIN_SECONDS) {
                return false;
            }
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
     */
    @Override
    public void recordResendAttempt(String email) {
        String attemptsKey = RESEND_ATTEMPTS_PREFIX + email;
        String lastTimeKey = RESEND_LAST_TIME_PREFIX + email;

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts == 1) {
            // Set expiration on first attempt (1 hour)
            redisTemplate.expire(attemptsKey, Duration.ofHours(1));
        }

        // Update last resend time
        redisTemplate.opsForValue().set(lastTimeKey, System.currentTimeMillis(), Duration.ofHours(1));

        log.debug("Recorded resend verification attempt for user: {}", email);
    }

    /**
     * Clear verification attempts after successful verification
     */
    @Override
    public void clearVerificationAttempts(String email) {
        String key = VERIFICATION_ATTEMPTS_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Cleared verification attempts for user: {}", email);
    }

    /**
     * Clear resend attempts
     */
    @Override
    public void clearResendAttempts(String email) {
        String attemptsKey = RESEND_ATTEMPTS_PREFIX + email;
        String lastTimeKey = RESEND_LAST_TIME_PREFIX + email;
        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(lastTimeKey);
        log.debug("Cleared resend attempts for user: {}", email);
    }
}
