package ir.netpick.platform.gatekeeper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityWebhookService {

    @Value("${security.webhook.enabled:false}")
    private boolean enabled;

    @Value("${security.webhook.url:}")
    private String webhookUrl;

    @Value("${security.webhook.secret:}")
    private String webhookSecret;

    private final ObjectMapper objectMapper;

    @Async
    public void sendWebhook(SecurityEvent event) {
        if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "event", event.getEventType(),
                    "user", event.getUserEmail() != null ? event.getUserEmail() : "unknown",
                    "ip", event.getIpAddress() != null ? event.getIpAddress() : "unknown",
                    "riskScore", event.getRiskScore(),
                    "blocked", event.isBlocked(),
                    "timestamp", event.getCreatedAt().toString(),
                    "details", event.getDetails() != null ? event.getDetails() : Map.of()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (webhookSecret != null && !webhookSecret.isBlank()) {
                headers.set("X-Webhook-Secret", webhookSecret);
            }

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.debug("Webhook sent for event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to send security webhook: {}", e.getMessage());
        }
    }
}
