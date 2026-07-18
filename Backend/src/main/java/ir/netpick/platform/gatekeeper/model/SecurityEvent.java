package ir.netpick.platform.gatekeeper.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "security_events", indexes = {
        @Index(name = "idx_security_events_type", columnList = "event_type"),
        @Index(name = "idx_security_events_user_id", columnList = "user_id"),
        @Index(name = "idx_security_events_ip", columnList = "ip_address"),
        @Index(name = "idx_security_events_created", columnList = "created_at"),
        @Index(name = "idx_security_events_risk", columnList = "risk_score")
})
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "geo_location")
    private String geoLocation;

    @Type(JsonBinaryType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "risk_score")
    private int riskScore = 0;

    @Column(name = "blocked", nullable = false)
    private boolean blocked = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGIN_LOCKED,
        LOGOUT,
        LOGOUT_ALL_DEVICES,
        SIGNUP,
        EMAIL_VERIFIED,
        PASSWORD_RESET_REQUESTED,
        PASSWORD_RESET_COMPLETED,
        PASSWORD_CHANGED,
        MFA_SETUP_STARTED,
        MFA_ENABLED,
        MFA_DISABLED,
        MFA_CODE_VERIFIED,
        MFA_BACKUP_CODE_USED,
        MFA_CODE_FAILED,
        TOKEN_REFRESHED,
        SESSION_CREATED,
        SESSION_REVOKED,
        ANOMALY_DETECTED,
        IP_BLOCKED,
        IP_POLICY_CREATED,
        IP_POLICY_UPDATED,
        IP_POLICY_DELETED,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        USER_CREATED,
        USER_DELETED,
        USER_RESTORED,
        ROLE_CHANGED,
        SUSPICIOUS_ACTIVITY
    }
}
