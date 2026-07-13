package ir.netpick.mailmine.email.model;

import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "email_campaigns", indexes = {
    @Index(name = "idx_campaigns_status", columnList = "status"),
    @Index(name = "idx_campaigns_scheduled", columnList = "scheduled_at")
})
@Getter
@Setter
public class Campaign extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "subject_line", nullable = false)
    private String subjectLine;

    @Column(name = "body_html", columnDefinition = "text", nullable = false)
    private String bodyHtml;

    @Column(name = "body_text", columnDefinition = "text")
    private String bodyText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "total_recipients", nullable = false)
    private Integer totalRecipients = 0;

    @Column(name = "total_sent", nullable = false)
    private Integer totalSent = 0;

    @Column(name = "total_opened", nullable = false)
    private Integer totalOpened = 0;

    @Column(name = "total_clicked", nullable = false)
    private Integer totalClicked = 0;

    @Column(name = "total_bounced", nullable = false)
    private Integer totalBounced = 0;

    @Column(name = "total_unsubscribed", nullable = false)
    private Integer totalUnsubscribed = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "is_ab_test", nullable = false)
    private Boolean isAbTest = false;

    @Column(name = "ab_variant")
    private String abVariant;

    public enum CampaignStatus {
        DRAFT,
        SCHEDULED,
        SENDING,
        SENT,
        PAUSED,
        CANCELLED,
        FAILED
    }
}
