package ir.netpick.mailmine.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, UUID> {

    List<AuditTrail> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);

    List<AuditTrail> findByPerformedByIdOrderByCreatedAtDesc(UUID userId);

    List<AuditTrail> findByCreatedAtAfter(LocalDateTime since);

    List<AuditTrail> findByEntityTypeAndCreatedAtAfterOrderByCreatedAtDesc(String entityType, LocalDateTime since);
}
