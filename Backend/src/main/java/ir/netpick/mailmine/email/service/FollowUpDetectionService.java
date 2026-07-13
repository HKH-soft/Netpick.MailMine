package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowUpDetectionService {

    private final EmailMessageRepository emailMessageRepository;

    private static final int FOLLOW_UP_THRESHOLD_HOURS = 48;
    private static final int URGENT_THRESHOLD_HOURS = 96;

    // SSE notification listeners
    private final Map<String, Consumer<Map<String, Object>>> notificationListeners = new ConcurrentHashMap<>();

    /**
     * Detect unanswered emails - runs every hour
     */
    @Scheduled(fixedDelay = 3600000)
    public void detectUnansweredEmails() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(FOLLOW_UP_THRESHOLD_HOURS);
        List<EmailMessage> unanswered = emailMessageRepository.findUnrepliedEmailsOlderThan(threshold);

        for (EmailMessage email : unanswered) {
            long hoursSinceReceived = ChronoUnit.HOURS.between(email.getReceivedAt(), LocalDateTime.now());
            boolean isUrgent = hoursSinceReceived >= URGENT_THRESHOLD_HOURS;

            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("type", "FOLLOW_UP_NEEDED");
            notification.put("emailId", email.getId().toString());
            notification.put("subject", email.getSubject());
            notification.put("sender", email.getSenderEmail());
            notification.put("hoursSinceReceived", hoursSinceReceived);
            notification.put("urgent", isUrgent);
            notification.put("assignedTo", email.getAssignedTo() != null
                    ? email.getAssignedTo().getId().toString() : null);
            notification.put("timestamp", LocalDateTime.now().toString());

            log.info("Follow-up needed: {} from {} ({} hours, urgent={})",
                    email.getSubject(), email.getSenderEmail(), hoursSinceReceived, isUrgent);

            // Notify listeners (SSE clients)
            notifyListeners(notification);
        }
    }

    /**
     * Register a notification listener for SSE
     */
    public String registerListener(Consumer<Map<String, Object>> listener) {
        String listenerId = UUID.randomUUID().toString();
        notificationListeners.put(listenerId, listener);
        return listenerId;
    }

    /**
     * Remove a notification listener
     */
    public void removeListener(String listenerId) {
        notificationListeners.remove(listenerId);
    }

    /**
     * Send notification to all registered listeners
     */
    private void notifyListeners(Map<String, Object> notification) {
        notificationListeners.forEach((id, listener) -> {
            try {
                listener.accept(notification);
            } catch (Exception e) {
                log.error("Failed to notify listener {}: {}", id, e.getMessage());
                notificationListeners.remove(id);
            }
        });
    }

    public long getHoursSinceLastReply(EmailMessage email) {
        if (email.getLastReplyAt() == null) {
            return ChronoUnit.HOURS.between(email.getReceivedAt(), LocalDateTime.now());
        }
        return ChronoUnit.HOURS.between(email.getLastReplyAt(), LocalDateTime.now());
    }

    public List<Map<String, Object>> getFollowUpDashboard() {
        List<Map<String, Object>> dashboard = new ArrayList<>();

        // Urgent (>96h)
        LocalDateTime urgentThreshold = LocalDateTime.now().minusHours(URGENT_THRESHOLD_HOURS);
        List<EmailMessage> urgent = emailMessageRepository.findUnrepliedEmailsOlderThan(urgentThreshold);
        for (EmailMessage email : urgent) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("emailId", email.getId().toString());
            item.put("subject", email.getSubject());
            item.put("sender", email.getSenderEmail());
            item.put("hoursSinceReceived", ChronoUnit.HOURS.between(email.getReceivedAt(), LocalDateTime.now()));
            item.put("priority", "URGENT");
            item.put("assignedTo", email.getAssignedTo() != null
                    ? email.getAssignedTo().getName() : "Unassigned");
            dashboard.add(item);
        }

        // Normal follow-up (48-96h)
        LocalDateTime normalThreshold = LocalDateTime.now().minusHours(FOLLOW_UP_THRESHOLD_HOURS);
        List<EmailMessage> normal = emailMessageRepository.findUnrepliedEmailsOlderThan(normalThreshold);
        for (EmailMessage email : normal) {
            long hours = ChronoUnit.HOURS.between(email.getReceivedAt(), LocalDateTime.now());
            if (hours < URGENT_THRESHOLD_HOURS) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("emailId", email.getId().toString());
                item.put("subject", email.getSubject());
                item.put("sender", email.getSenderEmail());
                item.put("hoursSinceReceived", hours);
                item.put("priority", "NORMAL");
                item.put("assignedTo", email.getAssignedTo() != null
                        ? email.getAssignedTo().getName() : "Unassigned");
                dashboard.add(item);
            }
        }

        return dashboard;
    }
}