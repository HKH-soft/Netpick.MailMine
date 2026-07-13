package ir.netpick.platform.mailmine.model;

import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "email_rules", indexes = {
    @Index(name = "idx_email_rules_active", columnList = "is_active"),
    @Index(name = "idx_email_rules_priority", columnList = "priority")
})
@Getter
@Setter
public class EmailRule extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;

    @Column(name = "condition_value", nullable = false)
    private String conditionValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "action_value")
    private String actionValue;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    public enum ConditionType {
        SENDER_CONTAINS,
        SUBJECT_CONTAINS,
        BODY_CONTAINS,
        HAS_ATTACHMENT,
        TAG_MATCHES
    }

    public enum ActionType {
        ASSIGN_TO_USER,
        ADD_TAG,
        MOVE_TO_FOLDER,
        MARK_AS_READ,
        SEND_NOTIFICATION
    }
}








