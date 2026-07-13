package ir.netpick.mailmine.email.repository;

import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.model.EmailMessage.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {

    Optional<EmailMessage> findByMessageId(String messageId);

    Page<EmailMessage> findBySenderEmailContainingIgnoreCase(String senderEmail, Pageable pageable);

    Page<EmailMessage> findByStatus(EmailStatus status, Pageable pageable);

    @Query("SELECT e FROM EmailMessage e WHERE e.receivedAt < :threshold AND e.isAnswered = false AND e.status = 'INBOX'")
    List<EmailMessage> findUnrepliedEmailsOlderThan(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT e FROM EmailMessage e JOIN e.emailTags t WHERE t.emailTag.id = :tagId")
    Page<EmailMessage> findByTagId(@Param("tagId") UUID tagId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM EmailMessage e WHERE e.receivedAt >= :startOfDay AND e.receivedAt < :endOfDay")
    Long countEmailsReceivedToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    Page<EmailMessage> findByAssignedToId(UUID assignedToId, Pageable pageable);

    List<EmailMessage> findByThreadIdOrderByReceivedAtAsc(String threadId);

    Page<EmailMessage> findBySubjectContainingIgnoreCaseOrSenderEmailContainingIgnoreCase(
            String subject, String senderEmail, Pageable pageable);

    List<EmailMessage> findByReceivedAtAfter(java.time.LocalDateTime date);
}