package ir.netpick.platform.mailmine.model;

import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "email_messages", indexes = {
    @Index(name = "idx_email_messages_message_id", columnList = "message_id"),
    @Index(name = "idx_email_messages_sender", columnList = "sender_email"),
    @Index(name = "idx_email_messages_received", columnList = "received_at"),
    @Index(name = "idx_email_messages_thread", columnList = "thread_id")
})
@Getter
@Setter
public class EmailMessage extends BaseEntity {

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "thread_id")
    private String threadId;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "sender_name")
    private String senderName;

    @ElementCollection
    @CollectionTable(name = "email_recipients", joinColumns = @JoinColumn(name = "email_message_id"))
    @Column(name = "recipient_email")
    private Set<String> recipients = new HashSet<>();

    @Column(name = "subject")
    private String subject;

    @Column(name = "body_text", columnDefinition = "text")
    private String bodyText;

    @Column(name = "body_html", columnDefinition = "text")
    private String bodyHtml;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_answered", nullable = false)
    private Boolean isAnswered = false;

    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged = false;

    @Column(name = "has_attachments", nullable = false)
    private Boolean hasAttachments = false;

    @Column(name = "mailbox_folder")
    private String mailboxFolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmailStatus status = EmailStatus.INBOX;

    @Column(name = "last_reply_at")
    private LocalDateTime lastReplyAt;

    @Column(name = "reply_due_at")
    private LocalDateTime replyDueAt;

    @OneToMany(mappedBy = "emailMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmailTagAssignment> emailTags = new HashSet<>();

    public enum EmailStatus {
        INBOX,
        ASSIGNED,
        REPLIED,
        CLOSED,
        SPAM,
        ARCHIVED
    }
}








