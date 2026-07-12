package ir.netpick.mailmine.auth.repository;

import ir.netpick.mailmine.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find all refresh tokens for a user.
     */
    List<RefreshToken> findByUserId(UUID userId);

    /**
     * Revoke all refresh tokens for a user.
     */
    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    int revokeAllByUserId(@Param("userId") UUID userId);

    /**
     * Delete expired refresh tokens (cleanup job).
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Delete revoked refresh tokens older than a certain time.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.createdAt < :before")
    int deleteRevokedTokensBefore(@Param("before") java.time.LocalDateTime before);

    /**
     * Check if a valid token exists for the user.
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    boolean hasValidToken(@Param("userId") UUID userId, @Param("now") Instant now);
}
