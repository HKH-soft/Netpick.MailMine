package ir.netpick.mailmine.email.model;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "email_tag_assignments", indexes = {
    @Index(name = "idx_tag_assignments_email", columnList = "email_message_id"),
    @Index(name = "idx_tag_assignments_tag", columnList = "email_tag_id")
})
@Getter
@Setter
public class EmailTagAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_message_id", nullable = false)
    private EmailMessage emailMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_tag_id", nullable = false)
    private EmailTag emailTag;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "is_ai_generated", nullable = false)
    private Boolean isAiGenerated = false;
}