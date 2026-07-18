package ir.netpick.platform.gatekeeper.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ip_policies")
public class IpPolicy extends BaseEntity {

    @Column(name = "policy_name", nullable = false)
    private String policyName;

    @Column(name = "policy_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "ip_range_start")
    private String ipRangeStart;

    @Column(name = "ip_range_end")
    private String ipRangeEnd;

    @Column(name = "cidr_notation")
    private String cidrNotation;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    public enum PolicyType {
        ALLOWLIST,
        BLOCKLIST
    }
}
