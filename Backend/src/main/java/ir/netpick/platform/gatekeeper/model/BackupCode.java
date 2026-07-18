package ir.netpick.platform.gatekeeper.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mfa_backup_codes", indexes = {
        @Index(name = "idx_mfa_backup_codes_user_id", columnList = "user_id")
})
public class BackupCode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
