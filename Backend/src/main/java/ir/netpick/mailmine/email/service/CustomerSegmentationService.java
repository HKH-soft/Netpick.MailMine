package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerSegmentationService {

    private final EmailMessageRepository emailMessageRepository;

    /**
     * Segment customers by activity level based on email frequency.
     */
    public Map<String, Object> segmentByActivity() {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

        // Get all unique senders from the last 90 days
        List<EmailMessage> recentEmails = emailMessageRepository
                .findByReceivedAtAfter(ninetyDaysAgo);

        Map<String, Long> senderCounts = recentEmails.stream()
                .collect(Collectors.groupingBy(EmailMessage::getSenderEmail, Collectors.counting()));

        // Active: 5+ emails in last 30 days
        List<String> active = senderCounts.entrySet().stream()
                .filter(e -> {
                    long count = recentEmails.stream()
                            .filter(m -> m.getSenderEmail().equals(e.getKey())
                                    && m.getReceivedAt().isAfter(thirtyDaysAgo))
                            .count();
                    return count >= 5;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Engaged: 2-4 emails in last 30 days
        List<String> engaged = senderCounts.entrySet().stream()
                .filter(e -> {
                    long count = recentEmails.stream()
                            .filter(m -> m.getSenderEmail().equals(e.getKey())
                                    && m.getReceivedAt().isAfter(thirtyDaysAgo))
                            .count();
                    return count >= 2 && count < 5;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // One-time: only 1 email
        List<String> oneTime = senderCounts.entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Inactive: emails older than 30 days but none recent
        List<String> inactive = senderCounts.entrySet().stream()
                .filter(e -> {
                    long recentCount = recentEmails.stream()
                            .filter(m -> m.getSenderEmail().equals(e.getKey())
                                    && m.getReceivedAt().isAfter(thirtyDaysAgo))
                            .count();
                    return recentCount == 0 && e.getValue() > 0;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        result.put("active", Map.of("count", active.size(), "emails", active));
        result.put("engaged", Map.of("count", engaged.size(), "emails", engaged));
        result.put("oneTime", Map.of("count", oneTime.size(), "emails", oneTime));
        result.put("inactive", Map.of("count", inactive.size(), "emails", inactive));
        result.put("totalUniqueSenders", senderCounts.size());

        return result;
    }

    /**
     * Get top customers by email volume.
     */
    public List<Map<String, Object>> getTopCustomers(int limit) {
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        List<EmailMessage> recentEmails = emailMessageRepository
                .findByReceivedAtAfter(ninetyDaysAgo);

        Map<String, Long> senderCounts = recentEmails.stream()
                .collect(Collectors.groupingBy(EmailMessage::getSenderEmail, Collectors.counting()));

        return senderCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> customer = new LinkedHashMap<>();
                    customer.put("email", e.getKey());
                    customer.put("emailCount", e.getValue());
                    // Calculate average response time for this sender
                    List<EmailMessage> senderEmails = recentEmails.stream()
                            .filter(m -> m.getSenderEmail().equals(e.getKey()))
                            .sorted(Comparator.comparing(EmailMessage::getReceivedAt))
                            .collect(Collectors.toList());
                    customer.put("firstSeen", senderEmails.isEmpty() ? null : senderEmails.get(0).getReceivedAt());
                    customer.put("lastSeen", senderEmails.isEmpty() ? null : senderEmails.get(senderEmails.size() - 1).getReceivedAt());
                    return customer;
                })
                .collect(Collectors.toList());
    }
}
