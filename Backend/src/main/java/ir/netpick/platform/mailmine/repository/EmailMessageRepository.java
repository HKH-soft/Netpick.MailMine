package ir.netpick.platform.mailmine.repository;

import ir.netpick.platform.mailmine.model.EmailMessage;
import ir.netpick.platform.mailmine.model.EmailMessage.EmailStatus;
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

    @Query("SELECT e FROM EmailMessage e WHERE e.emailTags IS EMPTY")
    Page<EmailMessage> findUnprocessedEmails(Pageable pageable);

    @Query("SELECT e FROM EmailMessage e WHERE e.receivedAt < :threshold AND e.isAnswered = false AND e.status = 'INBOX'")
    Page<EmailMessage> findUnrepliedEmailsOlderThan(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    Page<EmailMessage> findByReceivedAtAfter(java.time.LocalDateTime date, Pageable pageable);

    @Query("SELECT COUNT(e) FROM EmailMessage e WHERE e.isAnswered = :answered AND e.receivedAt >= :start AND e.receivedAt < :end")
    long countByIsAnsweredAndReceivedAtBetween(@Param("answered") boolean answered, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(e) FROM EmailMessage e WHERE e.isRead = :isRead AND e.receivedAt >= :start AND e.receivedAt < :end")
    long countByIsReadAndReceivedAtBetween(@Param("isRead") boolean isRead, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(e) FROM EmailMessage e WHERE e.isAnswered = false AND e.status = 'INBOX'")
    long countUnanswered();

    @Query("SELECT AVG(FUNCTION('EXTRACT', EPOCH FROM e.lastReplyAt) - FUNCTION('EXTRACT', EPOCH FROM e.receivedAt)) FROM EmailMessage e WHERE e.isAnswered = true AND e.lastReplyAt IS NOT NULL AND e.receivedAt >= :start AND e.receivedAt < :end")
    Double averageResponseTimeSecondsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e.senderEmail, COUNT(e) FROM EmailMessage e WHERE e.receivedAt >= :start AND e.receivedAt < :end GROUP BY e.senderEmail ORDER BY COUNT(e) DESC")
    List<Object[]> topSendersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT e FROM EmailMessage e WHERE e.isAnswered = true AND e.lastReplyAt IS NOT NULL AND e.receivedAt >= :start AND e.receivedAt < :end")
    List<EmailMessage> findAnsweredBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}








