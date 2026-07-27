package ir.netpick.platform.mailmine.service;

import ir.netpick.platform.mailmine.model.EmailMessage;
import ir.netpick.platform.mailmine.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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

    public Map<String, Object> getDailyStats(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Long receivedToday = emailMessageRepository.countEmailsReceivedToday(startOfDay, endOfDay);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("date", date.toString());
        stats.put("emailsReceived", receivedToday);
        stats.put("emailsReplied", emailMessageRepository.countByIsAnsweredAndReceivedAtBetween(true, startOfDay, endOfDay));
        stats.put("emailsRead", emailMessageRepository.countByIsReadAndReceivedAtBetween(true, startOfDay, endOfDay));
        stats.put("averageResponseTimeHours", calculateAverageResponseTime(startOfDay, endOfDay));

        return stats;
    }

    public Map<String, Object> getWeeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDateTime weekAgoStart = weekAgo.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        long totalReceived = emailMessageRepository.countEmailsReceivedToday(weekAgoStart, todayEnd);
        long totalReplied = emailMessageRepository.countByIsAnsweredAndReceivedAtBetween(true, weekAgoStart, todayEnd);

        Map<String, Object> weekly = new LinkedHashMap<>();
        weekly.put("period", weekAgo + " to " + today);
        weekly.put("totalReceived", totalReceived);
        weekly.put("totalReplied", totalReplied);
        weekly.put("topSenders", getTopSenders(weekAgoStart, todayEnd));
        weekly.put("unansweredCount", emailMessageRepository.countUnanswered());

        return weekly;
    }

    public List<Map<String, Object>> getTopSenders(LocalDateTime start, LocalDateTime end) {
        return emailMessageRepository.topSendersBetween(
                        start, end, PageRequest.of(0, TOP_SENDERS_LIMIT))
                .stream()
                .map(row -> Map.<String, Object>of(
                        "email", row[0],
                        "count", row[1]))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getResponseTimeMetrics() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(30).atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<EmailMessage> answered = emailMessageRepository.findAnsweredBetween(start, end);

        if (answered.isEmpty()) {
            return Map.of("averageHours", 0, "medianHours", 0, "p95Hours", 0, "sampleSize", 0);
        }

        List<Long> responseTimes = answered.stream()
                .map(e -> ChronoUnit.MINUTES.between(e.getReceivedAt(), e.getLastReplyAt()))
                .sorted()
                .toList();

        double averageMinutes = responseTimes.stream().mapToLong(l -> l).average().orElse(0);
        double average = averageMinutes / 60.0;
        long medianMinutes = responseTimes.get(responseTimes.size() / 2);
        int p95Index = (int) Math.ceil(responseTimes.size() * 0.95) - 1;
        long p95Minutes = responseTimes.get(Math.max(0, p95Index));

        return Map.of(
                "averageHours", Math.round(average * 10.0) / 10.0,
                "medianHours", Math.round(medianMinutes / 60.0 * 10.0) / 10.0,
                "p95Hours", Math.round(p95Minutes / 60.0 * 10.0) / 10.0,
                "sampleSize", responseTimes.size()
        );
    }

    public List<Map<String, Object>> getVolumeTrend() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(29).atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        // Single batch query instead of 30 individual daily queries (N+1 fix)
        List<Object[]> rows = emailMessageRepository.volumeTrendBetween(start, end);

        // Build a map of date -> counts
        Map<String, long[]> dateMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            LocalDate date = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            long received = ((Number) row[1]).longValue();
            long replied = ((Number) row[2]).longValue();
            long readCount = ((Number) row[3]).longValue();
            dateMap.put(date.toString(), new long[]{received, replied, readCount});
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String key = date.toString();
            long[] counts = dateMap.getOrDefault(key, new long[]{0, 0, 0});
            Map<String, Object> dayStats = new LinkedHashMap<>();
            dayStats.put("date", key);
            dayStats.put("emailsReceived", counts[0]);
            dayStats.put("emailsReplied", counts[1]);
            dayStats.put("emailsRead", counts[2]);
            dayStats.put("averageResponseTimeHours", 0.0);
            trend.add(dayStats);
        }

        return trend;
    }

    public Map<String, Object> getDashboardSummary() {
        LocalDate today = LocalDate.now();
        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("today", getDailyStats(today));
        summary.put("thisWeek", getWeeklyStats());
        summary.put("responseTime", getResponseTimeMetrics());
        summary.put("volumeTrend", getVolumeTrend());
        summary.put("unansweredCount", emailMessageRepository.countUnanswered());
        summary.put("topSenders", getTopSenders(
                today.minusDays(7).atStartOfDay(),
                today.atTime(LocalTime.MAX)));

        return summary;
    }

    private double calculateAverageResponseTime(LocalDateTime start, LocalDateTime end) {
        Double avgSeconds = emailMessageRepository.averageResponseTimeSecondsBetween(start, end);
        if (avgSeconds == null) return 0;
        return Math.round((avgSeconds / 3600.0) * 10.0) / 10.0;
    }
}










