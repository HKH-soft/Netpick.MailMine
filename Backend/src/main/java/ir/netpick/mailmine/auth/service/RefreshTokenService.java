package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.exception.InvalidTokenException;
import ir.netpick.mailmine.auth.model.RefreshToken;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens.
 * Tokens are hashed before storage to prevent exposure if database is compromised.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 64;

    @Value("${security.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    /**
     * Generate a new refresh token for a user.
     *
     * @param user       the user to generate token for
     * @param deviceInfo optional device information
     * @param ipAddress  optional IP address
     * @return the created RefreshToken (with raw token accessible via getToken())
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        log.debug("Creating refresh token for user: {}", user.getEmail());

        String tokenValue = generateSecureToken();
        Instant expiresAt = Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setTokenHash(passwordEncoder.encode(tokenValue));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}, expires: {}", user.getEmail(), expiresAt);
        return saved;
    }

    /**
     * Find a valid refresh token by its token string.
     * Uses token hash comparison for security.
     * Note: BCrypt hashes are non-deterministic, requiring iteration.
     * For better performance, consider using RedisRateLimitingService with token cache.
     *
     * @param token the raw token string
     * @return the RefreshToken if valid
     */
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findAll().stream()
                .filter(rt -> passwordEncoder.matches(token, rt.getTokenHash()))
                .filter(rt -> !rt.isRevoked() && !rt.isExpired())
                .findFirst();
    }

    /**
     * Verify and return the refresh token.
     *
     * @param token the raw token string
     * @return the RefreshToken if valid
     * @throws InvalidTokenException if token is invalid, expired, or revoked
     */
    public RefreshToken verifyRefreshToken(String token) {
        log.debug("Verifying refresh token");

        RefreshToken refreshToken = findValidToken(token).orElseThrow(() -> {
            log.warn("Refresh token not found or invalid");
            return new InvalidTokenException("Invalid refresh token");
        });

        log.debug("Refresh token verified for user: {}", refreshToken.getUser().getEmail());
        return refreshToken;
    }

    /**
     * Revoke a refresh token by its raw token string.
     *
     * @param token the raw token string to revoke
     */
    @Transactional
    public void revokeToken(String token) {
        log.debug("Revoking refresh token");
        // Find token by hash (including revoked/expired ones)
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findAll().stream()
                .filter(rt -> passwordEncoder.matches(token, rt.getTokenHash()))
                .findFirst();
        if (tokenOpt.isPresent()) {
            RefreshToken refreshToken = tokenOpt.get();
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("Refresh token revoked successfully");
        } else {
            log.warn("Refresh token not found for revocation");
        }
    }

    /**
     * Revoke all refresh tokens for a user (e.g., on password change or logout from all devices).
     *
     * @param userId the user ID
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);
        int count = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} refresh tokens for user: {}", count, userId);
    }

    /**
     * Rotate a refresh token (revoke old one and create new one).
     * This is a security best practice - each refresh token can only be used once.
     *
     * @param oldToken   the old raw token to revoke
     * @param user       the user
     * @param deviceInfo optional device info
     * @param ipAddress  optional IP address
     * @return the new RefreshToken
     */
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken, User user, String deviceInfo, String ipAddress) {
        log.debug("Rotating refresh token for user: {}", user.getEmail());

        // Revoke the old token
        revokeToken(oldToken);

        // Create a new token
        return createRefreshToken(user, deviceInfo, ipAddress);
    }

    /**
     * Generate a cryptographically secure random token.
     *
     * @return a base64-encoded secure token
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Scheduled job to clean up expired and old revoked tokens.
     * Runs daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting refresh token cleanup job");

        // Delete expired tokens
        int expiredCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Deleted {} expired refresh tokens", expiredCount);

        // Delete revoked tokens older than 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int revokedCount = refreshTokenRepository.deleteRevokedTokensBefore(thirtyDaysAgo);
        log.info("Deleted {} old revoked refresh tokens", revokedCount);
    }
}
