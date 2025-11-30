package ir.netpick.mailmine.auth.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import ir.netpick.mailmine.auth.PreferencesEnum;
import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email")
}, uniqueConstraints = {
        @UniqueConstraint(name = "users_email_key", columnNames = { "email" })
})
public class User extends BaseEntity implements UserDetails {

    public User() {

    }

    public User(String email, String passwordHash, String name, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
    }

    @Column
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", length = Integer.MAX_VALUE)
    private String passwordHash;

    @Column(name = "profile_image_key")
    private String profileImageKey;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Enumerated(EnumType.STRING)
    private Map<PreferencesEnum, String> preferences;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Embedded
    private Verification verification;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.getName());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

}
