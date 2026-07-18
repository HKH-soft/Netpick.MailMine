package ir.netpick.platform.gatekeeper.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "device_sessions", indexes = {
        @Index(name = "idx_device_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_device_sessions_fingerprint", columnList = "device_fingerprint")
})
public class DeviceSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "geo_location")
    private String geoLocation;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked = false;
}
