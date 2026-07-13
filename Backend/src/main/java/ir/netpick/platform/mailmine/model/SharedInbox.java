package ir.netpick.platform.mailmine.model;

import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shared_inboxes", indexes = {
    @Index(name = "idx_shared_inboxes_email", columnList = "email_address")
})
@Getter
@Setter
public class SharedInbox extends BaseEntity {

    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "shared_inbox_members",
        joinColumns = @JoinColumn(name = "shared_inbox_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    public enum SharedInboxType {
        SALES,
        SUPPORT,
        INFO,
        JOBS
    }
}








