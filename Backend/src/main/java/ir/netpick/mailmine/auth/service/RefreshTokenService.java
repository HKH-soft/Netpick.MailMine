package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.model.RefreshToken;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RefreshTokenRepository;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

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
     * @return the created RefreshToken
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        log.debug("Creating refresh token for user: {}", user.getEmail());

        String tokenValue = generateSecureToken();
        Instant expiresAt = Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS);

        RefreshToken refreshToken = new RefreshToken(tokenValue, user, expiresAt, deviceInfo, ipAddress);
        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.info("Created refresh token for user: {}, expires: {}", user.getEmail(), expiresAt);
        return saved;
    }

    /**
     * Find a valid refresh token by its token string.
     *
     * @param token the token string
     * @return the RefreshToken if valid
     */
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findValidToken(token, Instant.now());
    }

    /**
     * Verify and return the refresh token.
     *
     * @param token the token string
     * @return the RefreshToken if valid
     * @throws ResourceNotFoundException if token is invalid, expired, or revoked
     */
    public RefreshToken verifyRefreshToken(String token) {
        log.debug("Verifying refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found");
                    return new ResourceNotFoundException("Invalid refresh token");
                });

        if (refreshToken.isRevoked()) {
            log.warn("Refresh token is revoked for user: {}", refreshToken.getUser().getEmail());
            throw new ResourceNotFoundException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            log.warn("Refresh token is expired for user: {}", refreshToken.getUser().getEmail());
            throw new ResourceNotFoundException("Refresh token has expired");
        }

        log.debug("Refresh token verified for user: {}", refreshToken.getUser().getEmail());
        return refreshToken;
    }

    /**
     * Revoke a refresh token.
     *
     * @param token the token string to revoke
     */
    @Transactional
    public void revokeToken(String token) {
        log.debug("Revoking refresh token");
        int updated = refreshTokenRepository.revokeByToken(token);
        if (updated > 0) {
            log.info("Refresh token revoked successfully");
        } else {
            log.warn("Refresh token not found for revocation");
        }
    }

    /**
     * Revoke all refresh tokens for a user (e.g., on password change or logout from
     * all devices).
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
     * @param oldToken   the old token to revoke
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
