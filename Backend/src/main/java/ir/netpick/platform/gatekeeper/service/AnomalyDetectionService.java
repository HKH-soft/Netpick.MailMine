package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final SecurityEventService securityEventService;

    @Value("${security.anomaly.enabled:true}")
    private boolean enabled;

    @Value("${security.anomaly.max-failures-per-hour:10}")
    private int maxFailuresPerHour;

    @Value("${security.anomaly.risk-score-threshold:50}")
    private int riskThreshold;

public AnomalyAnalysis analyzeLogin(String email, String ipAddress, String deviceFingerprint,
                                         boolean loginSuccessful) {
        int riskScore = 0;
        StringBuilder reasons = new StringBuilder();

        if (!loginSuccessful) {
            riskScore += 10;
            if (reasons.length() > 0) reasons.append("; ");
            reasons.append("Failed login attempt");
        }

        boolean blocked = riskScore >= riskThreshold;

        if (riskScore > 0) {
            securityEventService.logEventSync(
                    blocked ? SecurityEvent.EventType.SUSPICIOUS_ACTIVITY : SecurityEvent.EventType.ANOMALY_DETECTED,
                    null, email, ipAddress, null, deviceFingerprint,
                    Map.of("riskScore", riskScore, "reasons", reasons.toString()),
                    riskScore, blocked);
        }

        if (blocked) {
            log.warn("Login blocked for {} from {} - risk score: {} ({})",
                    email, ipAddress, riskScore, reasons);
        }

        return new AnomalyAnalysis(riskScore, blocked, reasons.toString());
    }

    public record AnomalyAnalysis(int riskScore, boolean blocked, String reasons) {}
}
