package ir.netpick.platform.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@SQLDelete(sql = "UPDATE %{tableName} SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    protected UUID id;

    @Column(name = "description")
    protected String description;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    protected Boolean deleted = false;

}








