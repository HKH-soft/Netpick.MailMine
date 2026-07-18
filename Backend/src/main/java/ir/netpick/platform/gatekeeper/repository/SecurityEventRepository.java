package ir.netpick.platform.gatekeeper.repository;

import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    Page<SecurityEvent> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<SecurityEvent> findByEventTypeOrderByCreatedAtDesc(String eventType, Pageable pageable);

    Page<SecurityEvent> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);

    @Query("SELECT se FROM SecurityEvent se WHERE se.createdAt >= :since ORDER BY se.createdAt DESC")
    Page<SecurityEvent> findRecentEvents(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT se FROM SecurityEvent se WHERE se.riskScore >= :minRisk ORDER BY se.riskScore DESC, se.createdAt DESC")
    Page<SecurityEvent> findHighRiskEvents(@Param("minRisk") int minRisk, Pageable pageable);

    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.eventType = :eventType AND se.createdAt >= :since")
    long countByTypeSince(@Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.ipAddress = :ip AND se.eventType = :eventType AND se.createdAt >= :since")
    long countByIpAndTypeSince(@Param("ip") String ip, @Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT se.ipAddress, COUNT(se) FROM SecurityEvent se WHERE se.eventType = :eventType AND se.createdAt >= :since GROUP BY se.ipAddress ORDER BY COUNT(se) DESC")
    List<Object[]> findTopAttackIps(@Param("eventType") String eventType, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT se.userId) FROM SecurityEvent se WHERE se.eventType = :eventType AND se.createdAt >= :since")
    long countDistinctUsersByTypeSince(@Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.blocked = true AND se.createdAt >= :since")
    long countBlockedSince(@Param("since") LocalDateTime since);
}
