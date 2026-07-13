package ir.netpick.platform.mailmine.service;

import ir.netpick.platform.mailmine.dto.EmailRequest;
import ir.netpick.platform.mailmine.model.EmailQueueItem;
import ir.netpick.platform.mailmine.repository.EmailQueueItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {

    private final EmailQueueItemRepository emailQueueItemRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String QUEUE_KEY = "email_queue:pending";
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;

    /**
     * Queue an email for sending
     */
    public UUID queueEmail(EmailRequest request, UUID userId) {
        EmailQueueItem item = new EmailQueueItem();
        item.setRecipient(request.getRecipient());
        item.setSubject(request.getSubject());
        item.setBody(request.getBody());
        item.setAttachment(request.getAttachment());
        item.setCreatedByUserId(userId);
        item.setStatus(EmailQueueItem.QueueStatus.PENDING);
        item.setRetryCount(0);
        
        EmailQueueItem saved = emailQueueItemRepository.save(item);
        redisTemplate.opsForZSet().add(QUEUE_KEY, saved.getId().toString(), 
                System.currentTimeMillis() + saved.getPriority().getDelayMillis());
        
        return saved.getId();
    }

    /**
     * Process pending emails - runs every minute
     */
    @Scheduled(fixedDelay = 60000)
    public void processQueue() {
        List<EmailQueueItem> pending = emailQueueItemRepository
                .findTopNByStatusOrderByCreatedAtAsc(EmailQueueItem.QueueStatus.PENDING, 
                        PageRequest.of(0, BATCH_SIZE));

        for (EmailQueueItem item : pending) {
            try {
                processItem(item);
                item.setStatus(EmailQueueItem.QueueStatus.SENT);
                item.setSentAt(LocalDateTime.now());
                emailQueueItemRepository.save(item);
                redisTemplate.opsForZSet().remove(QUEUE_KEY, item.getId().toString());
            } catch (Exception e) {
                handleFailure(item, e);
            }
        }
    }

    private void processItem(EmailQueueItem item) {
        EmailRequest request = new EmailRequest();
        request.setRecipient(item.getRecipient());
        request.setSubject(item.getSubject());
        request.setBody(item.getBody());
        request.setAttachment(item.getAttachment());
        request.setRecipients(item.getRecipients());
        
        if (item.getAttachment() != null) {
            emailService.sendMailWithAttachment(request);
        } else {
            emailService.sendSimpleMail(request);
        }
    }

    private void handleFailure(EmailQueueItem item, Exception e) {
        item.setRetryCount(item.getRetryCount() + 1);
        
        if (item.getRetryCount() >= MAX_RETRIES) {
            item.setStatus(EmailQueueItem.QueueStatus.FAILED);
            item.setLastError(e.getMessage());
        } else {
            item.setStatus(EmailQueueItem.QueueStatus.PENDING);
            // Exponential backoff: retry in 5min, 10min, 20min
            long delay = (long) Math.pow(2, item.getRetryCount()) * 300;
            redisTemplate.opsForZSet().add(QUEUE_KEY, item.getId().toString(), 
                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delay));
        }
        
        emailQueueItemRepository.save(item);
        log.error("Email queue item {} failed (attempt {}): {}", 
                item.getId(), item.getRetryCount(), e.getMessage());
    }
}








