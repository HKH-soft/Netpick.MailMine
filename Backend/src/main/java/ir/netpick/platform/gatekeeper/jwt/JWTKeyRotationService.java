package ir.netpick.platform.gatekeeper.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Key;
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

    public JWTKeyRotationService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void initializeKeys() {
        activeKeyId = UUID.randomUUID().toString();
        activeSigningKey = Keys.hmacShaKeyFor(primarySecretKey.getBytes());
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
        return verificationKeys.get(keyId);
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
    }

    private void performKeyRotation() {
        // Archive current key for grace period
        verificationKeys.put(activeKeyId, activeSigningKey);
        log.info("Archived old JWT key: {}", activeKeyId);

        // Generate new key
        String oldKeyId = activeKeyId;
        activeKeyId = UUID.randomUUID().toString();
        activeSigningKey = Keys.hmacShaKeyFor(primarySecretKey.getBytes());

        // Update Redis
        redisTemplate.opsForValue().set("jwt:active-key-id", activeKeyId);
        redisTemplate.opsForValue().set("jwt:key-created-at", String.valueOf(System.currentTimeMillis()));

        // Schedule cleanup of old key after grace period
        cleanupOldKeyAfterGracePeriod(oldKeyId);

        log.info("Rotated JWT signing key. Old: {}, New: {}", oldKeyId, activeKeyId);
    }

    private void cleanupOldKeyAfterGracePeriod(String oldKeyId) {
        // Schedule removal after grace period using async scheduler
        scheduler.schedule(() -> {
            verificationKeys.remove(oldKeyId);
            log.info("Removed expired JWT key after grace period: {}", oldKeyId);
        }, gracePeriodDays, TimeUnit.DAYS);
    }
}