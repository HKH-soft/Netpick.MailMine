package ir.netpick.mailmine.email.model;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "email_queue_items", indexes = {
    @Index(name = "idx_email_queue_status", columnList = "status"),
    @Index(name = "idx_email_queue_priority", columnList = "priority"),
    @Index(name = "idx_email_queue_created", columnList = "created_at")
})
@Getter
@Setter
public class EmailQueueItem extends BaseEntity {

    @Column
    private String recipient;

    @Column
    private String subject;

    @Column(columnDefinition = "text")
    private String body;

    @Column
    private String attachment;

    @ElementCollection
    @CollectionTable(name = "email_queue_recipients", joinColumns = @JoinColumn(name = "email_queue_item_id"))
    @Column(name = "recipient_email")
    private List<String> recipients;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueuePriority priority = QueuePriority.NORMAL;

    public enum QueueStatus {
        PENDING,
        SENT,
        FAILED,
        CANCELLED
    }

    public enum QueuePriority {
        HIGH(0),
        NORMAL(1),
        LOW(2);

        private final int delayMillis;

        QueuePriority(int delayMillis) {
            this.delayMillis = delayMillis;
        }

        public int getDelayMillis() {
            return delayMillis;
        }
    }
}