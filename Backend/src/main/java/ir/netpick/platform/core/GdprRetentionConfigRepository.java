package ir.netpick.platform.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GdprRetentionConfigRepository extends JpaRepository<GdprRetentionConfig, UUID> {

    Optional<GdprRetentionConfig> findByEntityType(String entityType);

    List<GdprRetentionConfig> findByIsActiveTrue();
}








