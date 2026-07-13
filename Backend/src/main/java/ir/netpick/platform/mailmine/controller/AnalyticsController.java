package ir.netpick.platform.mailmine.controller;

import ir.netpick.platform.mailmine.service.MailAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mailmine/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AnalyticsController {

    private final MailAnalyticsService mailAnalyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(mailAnalyticsService.getDashboardSummary());
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(mailAnalyticsService.getDailyStats(date));
    }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStats() {
        return ResponseEntity.ok(mailAnalyticsService.getWeeklyStats());
    }

    @GetMapping("/response-times")
    public ResponseEntity<Map<String, Object>> getResponseTimeMetrics() {
        return ResponseEntity.ok(mailAnalyticsService.getResponseTimeMetrics());
    }

    @GetMapping("/volume-trend")
    public ResponseEntity<List<Map<String, Object>>> getVolumeTrend() {
        return ResponseEntity.ok(mailAnalyticsService.getVolumeTrend());
    }
}









