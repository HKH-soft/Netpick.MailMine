package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailAnalyticsService {

    private final EmailMessageRepository emailMessageRepository;

    private static final int TOP_SENDERS_LIMIT = 10;

    /**
     * Get daily email statistics
     */
    public Map<String, Object> getDailyStats(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Long receivedToday = emailMessageRepository.countEmailsReceivedToday(startOfDay, endOfDay);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("date", date.toString());
        stats.put("emailsReceived", receivedToday);
        stats.put("emailsReplied", countReplied(startOfDay, endOfDay));
        stats.put("emailsRead", countRead(startOfDay, endOfDay));
        stats.put("averageResponseTimeHours", calculateAverageResponseTime(startOfDay, endOfDay));

        return stats;
    }

    /**
     * Get weekly analytics summary
     */
    public Map<String, Object> getWeeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();
        for (LocalDate date = weekAgo; !date.isAfter(today); date = date.plusDays(1)) {
            dailyBreakdown.add(getDailyStats(date));
        }

        Map<String, Object> weekly = new LinkedHashMap<>();
        weekly.put("period", weekAgo + " to " + today);
        weekly.put("totalReceived", dailyBreakdown.stream()
                .mapToLong(d -> (Long) d.getOrDefault("emailsReceived", 0L))
                .sum());
        weekly.put("totalReplied", dailyBreakdown.stream()
                .mapToLong(d -> (Long) d.getOrDefault("emailsReplied", 0L))
                .sum());
        weekly.put("dailyBreakdown", dailyBreakdown);
        weekly.put("topSenders", getTopSenders(weekAgo.atStartOfDay(), today.atTime(LocalTime.MAX)));
        weekly.put("unansweredCount", countUnanswered());

        return weekly;
    }

    /**
     * Get top senders by email volume
     */
    public List<Map<String, Object>> getTopSenders(LocalDateTime start, LocalDateTime end) {
        // This would need a custom query - simplified for now
        List<EmailMessage> emails = emailMessageRepository.findAll(
                PageRequest.of(0, 1000)).getContent();

        return emails.stream()
                .filter(e -> e.getReceivedAt().isAfter(start) && e.getReceivedAt().isBefore(end))
                .collect(Collectors.groupingBy(
                        EmailMessage::getSenderEmail,
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_SENDERS_LIMIT)
                .map(entry -> Map.<String, Object>of(
                        "email", entry.getKey(),
                        "count", entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Get response time metrics
     */
    public Map<String, Object> getResponseTimeMetrics() {
        List<EmailMessage> answered = emailMessageRepository.findAll(
                PageRequest.of(0, 1000)).getContent().stream()
                .filter(e -> e.getIsAnswered() && e.getLastReplyAt() != null)
                .toList();

        if (answered.isEmpty()) {
            return Map.of("averageHours", 0, "medianHours", 0, "p95Hours", 0);
        }

        List<Long> responseTimes = answered.stream()
                .map(e -> ChronoUnit.HOURS.between(e.getReceivedAt(), e.getLastReplyAt()))
                .sorted()
                .toList();

        double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long median = responseTimes.get(responseTimes.size() / 2);
        long p95 = responseTimes.get((int) (responseTimes.size() * 0.95));

        return Map.of(
                "averageHours", Math.round(average * 10.0) / 10.0,
                "medianHours", median,
                "p95Hours", p95,
                "sampleSize", responseTimes.size()
        );
    }

    /**
     * Get email volume trend (last 30 days)
     */
    public List<Map<String, Object>> getVolumeTrend() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> dayStats = getDailyStats(date);
            trend.add(dayStats);
        }

        return trend;
    }

    /**
     * Get dashboard summary
     */
    public Map<String, Object> getDashboardSummary() {
        LocalDate today = LocalDate.now();
        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("today", getDailyStats(today));
        summary.put("thisWeek", getWeeklyStats());
        summary.put("responseTime", getResponseTimeMetrics());
        summary.put("volumeTrend", getVolumeTrend());
        summary.put("unansweredCount", countUnanswered());
        summary.put("topSenders", getTopSenders(
                today.minusDays(7).atStartOfDay(),
                today.atTime(LocalTime.MAX)));

        return summary;
    }

    private long countReplied(LocalDateTime start, LocalDateTime end) {
        return emailMessageRepository.findAll(PageRequest.of(0, 10000)).getContent().stream()
                .filter(e -> e.getIsAnswered())
                .filter(e -> e.getReceivedAt().isAfter(start) && e.getReceivedAt().isBefore(end))
                .count();
    }

    private long countRead(LocalDateTime start, LocalDateTime end) {
        return emailMessageRepository.findAll(PageRequest.of(0, 10000)).getContent().stream()
                .filter(e -> e.getIsRead())
                .filter(e -> e.getReceivedAt().isAfter(start) && e.getReceivedAt().isBefore(end))
                .count();
    }

    private double calculateAverageResponseTime(LocalDateTime start, LocalDateTime end) {
        List<EmailMessage> emails = emailMessageRepository.findAll(
                PageRequest.of(0, 10000)).getContent().stream()
                .filter(e -> e.getIsAnswered() && e.getLastReplyAt() != null)
                .filter(e -> e.getReceivedAt().isAfter(start) && e.getReceivedAt().isBefore(end))
                .toList();

        if (emails.isEmpty()) return 0;

        double avgHours = emails.stream()
                .mapToLong(e -> ChronoUnit.HOURS.between(e.getReceivedAt(), e.getLastReplyAt()))
                .average()
                .orElse(0);

        return Math.round(avgHours * 10.0) / 10.0;
    }

    private long countUnanswered() {
        return emailMessageRepository.findAll(PageRequest.of(0, 10000)).getContent().stream()
                .filter(e -> !e.getIsAnswered())
                .filter(e -> e.getStatus() == EmailMessage.EmailStatus.INBOX)
                .count();
    }
}
