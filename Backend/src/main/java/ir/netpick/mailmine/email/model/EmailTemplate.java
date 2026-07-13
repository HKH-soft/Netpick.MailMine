package ir.netpick.mailmine.email.model;

import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "email_templates", indexes = {
    @Index(name = "idx_email_templates_name", columnList = "name"),
    @Index(name = "idx_email_templates_category", columnList = "category")
})
@Getter
@Setter
public class EmailTemplate extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateCategory category;

    @Column(name = "subject_template", nullable = false)
    private String subjectTemplate;

    @Column(name = "body_template", columnDefinition = "text", nullable = false)
    private String bodyTemplate;

    @Column(name = "is_shared", nullable = false)
    private Boolean isShared = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    public enum TemplateCategory {
        WELCOME,
        QUOTE,
        SUPPORT,
        REMINDER,
        THANK_YOU,
        NEWSLETTER,
        PROMOTIONAL,
        CUSTOM
    }
}