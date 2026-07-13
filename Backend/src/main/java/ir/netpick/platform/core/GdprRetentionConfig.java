package ir.netpick.platform.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gdpr_retention_config")
@Getter
@Setter
public class GdprRetentionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, unique = true)
    private String entityType;

    @Column(name = "retention_days", nullable = false)
    private Integer retentionDays = 365;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_cleanup_at")
    private LocalDateTime lastCleanupAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}








