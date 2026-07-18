package ir.netpick.platform.gatekeeper.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mfa_settings", indexes = {
        @Index(name = "idx_mfa_settings_user_id", columnList = "user_id")
})
public class MfaSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "totp_secret")
    private String totpSecret;

    @Column(name = "totp_verified", nullable = false)
    private boolean totpVerified = false;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
