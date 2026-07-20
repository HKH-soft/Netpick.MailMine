package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private SecurityEventService securityEventService;

    private AnomalyDetectionService anomalyDetectionService;

    @BeforeEach
    void setUp() {
        anomalyDetectionService = new AnomalyDetectionService(securityEventService);
        setField(anomalyDetectionService, "riskThreshold", 50);
        setField(anomalyDetectionService, "maxFailuresPerHour", 10);
        setField(anomalyDetectionService, "enabled", true);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("analyzeLogin Tests")
    class AnalyzeLoginTests {
        @Test
        @DisplayName("Should calculate risk score for failed login")
        void shouldCalculateRiskForFailedLogin() {
            var result = anomalyDetectionService.analyzeLogin(
                    "user@test.com", "127.0.0.1", null, false);

            assertEquals(10, result.riskScore());
            assertFalse(result.blocked());
            verify(securityEventService).logEventSync(
                    any(SecurityEvent.EventType.class),
                    any(), anyString(), anyString(), nullable(String.class), nullable(String.class),
                    any(), anyInt(), anyBoolean());
        }

        @Test
        @DisplayName("Should log event for failed login")
        void shouldLogEventForFailedLogin() {
            anomalyDetectionService.analyzeLogin(
                    "user@test.com", "127.0.0.1", "fingerprint", false);

            verify(securityEventService).logEventSync(
                    any(SecurityEvent.EventType.class),
                    any(), anyString(), anyString(), nullable(String.class), anyString(),
                    any(), anyInt(), anyBoolean());
        }
    }
}