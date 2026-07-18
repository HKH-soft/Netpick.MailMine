package ir.netpick.platform.gatekeeper.repository;

import ir.netpick.platform.gatekeeper.model.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {

    List<DeviceSession> findByUserIdAndRevokedFalseAndDeletedFalse(UUID userId);

    Optional<DeviceSession> findByRefreshTokenIdAndRevokedFalse(UUID refreshTokenId);

    @Modifying
    @Query("UPDATE DeviceSession ds SET ds.revoked = true WHERE ds.user.id = :userId AND ds.revoked = false")
    int revokeAllByUserId(UUID userId);

    @Modifying
    @Query("UPDATE DeviceSession ds SET ds.revoked = true WHERE ds.id = :sessionId")
    int revokeById(UUID sessionId);

    long countByUserIdAndRevokedFalseAndDeletedFalse(UUID userId);

    @Query("SELECT ds FROM DeviceSession ds WHERE ds.user.id = :userId AND ds.revoked = false AND ds.deleted = false ORDER BY ds.lastActiveAt DESC")
    List<DeviceSession> findActiveSessionsByUserId(UUID userId);

    @Modifying
    @Query("UPDATE DeviceSession ds SET ds.lastActiveAt = :now WHERE ds.id = :sessionId")
    int updateLastActive(UUID sessionId, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM DeviceSession ds WHERE ds.expiresAt < CURRENT_TIMESTAMP OR ds.revoked = true")
    int cleanupExpiredSessions();
}
