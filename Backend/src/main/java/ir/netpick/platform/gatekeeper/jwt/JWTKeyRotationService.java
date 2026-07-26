package ir.netpick.platform.gatekeeper.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for JWT key rotation with grace period support.
 * Maintains multiple signing keys and rotates them periodically.
 * Old keys are kept for verification during grace period.
 */
@Slf4j
@Service
public class JWTKeyRotationService {

    @Value("${security.jwt.secret-key}")
    private String primarySecretKey;

    @Value("${security.jwt.key-rotation-days:30}")
    private int keyRotationDays;

    @Value("${security.jwt.grace-period-days:7}")
    private int gracePeriodDays;

    private final RedisTemplate<String, Object> redisTemplate;

    // Current active key
    private volatile Key activeSigningKey;
    private volatile String activeKeyId;

    // Old keys kept for verification during grace period
    private final ConcurrentHashMap<String, Key> verificationKeys = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int JWT_KEY_LENGTH = 256; // bits
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public JWTKeyRotationService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void initializeKeys() {
        // Validate minimum key length for HS256 (must be 256 bits = 32 bytes)
        if (primarySecretKey == null || primarySecretKey.length() < 32) {
            throw new IllegalStateException("JWT secret key must be at least 32 characters (256 bits) for HS256 algorithm. "
                    + "Set security.jwt.secret-key environment variable.");
        }
        activeKeyId = UUID.randomUUID().toString();
        activeSigningKey = Keys.hmacShaKeyFor(primarySecretKey.getBytes(StandardCharsets.UTF_8));
        log.info("Initialized JWT signing key with ID: {}", activeKeyId);
    }

    /**
     * Get the current signing key
     */
    public Key getSigningKey() {
        return activeSigningKey;
    }

    /**
     * Get the current key ID for token identification
     */
    public String getKeyId() {
        return activeKeyId;
    }

    /**
     * Get verification key for a specific key ID
     */
    public Key getVerificationKey(String keyId) {
        if (activeKeyId.equals(keyId)) {
            return activeSigningKey;
        }
        
        Key key = verificationKeys.get(keyId);
        if (key != null) {
            return key;
        }
        
        // Fallback: check Redis for old keys (cross-instance support)
        Object redisKey = redisTemplate.opsForValue().get("jwt:keys:" + keyId);
        if (redisKey instanceof byte[] keyBytes) {
            Key restoredKey = Keys.hmacShaKeyFor(keyBytes);
            verificationKeys.put(keyId, restoredKey);
            return restoredKey;
        }
        
        return null;
    }

    /**
     * Rotate signing key periodically
     */
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void rotateKeyIfNeeded() {
        String storedKeyId = (String) redisTemplate.opsForValue().get("jwt:active-key-id");
        String storedTimestamp = (String) redisTemplate.opsForValue().get("jwt:key-created-at");

        if (storedKeyId == null) {
            // First time setup
            storeActiveKey();
            return;
        }

        // Check if rotation needed
        if (storedTimestamp != null) {
            long createdAt = Long.parseLong(storedTimestamp);
            long now = System.currentTimeMillis();
            long rotationMs = Duration.ofDays(keyRotationDays).toMillis();

            if (now - createdAt > rotationMs) {
                performKeyRotation();
            }
        }
    }

    private void storeActiveKey() {
        redisTemplate.opsForValue().set("jwt:active-key-id", activeKeyId);
        redisTemplate.opsForValue().set("jwt:key-created-at", String.valueOf(System.currentTimeMillis()));
        
        // Persist active key bytes to Redis for cross-instance verification
        if (activeSigningKey != null) {
            byte[] keyBytes = activeSigningKey.getEncoded();
            redisTemplate.opsForValue().set("jwt:keys:" + activeKeyId, keyBytes);
        }
    }

    private void performKeyRotation() {
        String oldKeyId = activeKeyId;
        
        // Persist old key to Redis for cross-instance verification
        if (activeSigningKey != null) {
            byte[] oldKeyBytes = activeSigningKey.getEncoded();
            redisTemplate.opsForValue().set("jwt:keys:" + oldKeyId, oldKeyBytes);
            redisTemplate.expire("jwt:keys:" + oldKeyId, Duration.ofDays(gracePeriodDays));
        }
        
        // Archive current key for in-memory verification
        verificationKeys.put(activeKeyId, activeSigningKey);

        // Generate NEW random key for actual key rotation
        activeKeyId = UUID.randomUUID().toString();
        activeSigningKey = Keys.hmacShaKeyFor(generateRandomKeyBytes());

        // Update Redis
        redisTemplate.opsForValue().set("jwt:active-key-id", activeKeyId);
        redisTemplate.opsForValue().set("jwt:key-created-at", String.valueOf(System.currentTimeMillis()));

        // Schedule cleanup of old key after grace period
        cleanupOldKeyAfterGracePeriod(oldKeyId);

        log.info("Rotated JWT signing key. Old: {}, New: {}", oldKeyId, activeKeyId);
    }

    /**
     * Generate a cryptographically secure random key for JWT signing.
     */
    private byte[] generateRandomKeyBytes() {
        byte[] keyBytes = new byte[JWT_KEY_LENGTH / 8]; // 256 bits = 32 bytes
        SECURE_RANDOM.nextBytes(keyBytes);
        return keyBytes;
    }

    private void cleanupOldKeyAfterGracePeriod(String oldKeyId) {
        // Schedule removal after grace period using async scheduler
        scheduler.schedule(() -> {
            verificationKeys.remove(oldKeyId);
            log.info("Removed expired JWT key after grace period: {}", oldKeyId);
        }, gracePeriodDays, TimeUnit.DAYS);
    }
}