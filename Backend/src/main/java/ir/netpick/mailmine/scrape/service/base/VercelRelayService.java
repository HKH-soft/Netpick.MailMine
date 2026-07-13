package ir.netpick.mailmine.scrape.service.base;

import ir.netpick.mailmine.common.enums.ProxyProtocol;
import ir.netpick.mailmine.scrape.model.Proxy;
import ir.netpick.mailmine.scrape.repository.ProxyRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Vercel Relay deployments for proxying traffic through Vercel's network.
 * Uses Vercel API to deploy a relay worker and provides SOCKS5 proxy endpoint.
 */
@Slf4j
@Service
public class VercelRelayService {

    private final ProxyRepository proxyRepository;
    private final RestTemplate restTemplate;

    @Value("${vercel.token:}")
    private String vercelToken;

    @Value("${vercel.team-id:}")
    private String vercelTeamId;

    @Value("${vercel.relay-worker-url:https://vercel-relay.vercel.app}")
    private String relayWorkerUrl;

    // Active relay sessions: proxyId -> sessionId
    private Map<UUID, String> activeSessions = new ConcurrentHashMap<>();

    public VercelRelayService(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Deploy a Vercel Relay worker for the given proxy
     */
    public String deployRelay(Proxy proxy) {
        if (!ProxyProtocol.VERCEL_RELAY.equals(proxy.getProtocol())) {
            throw new IllegalArgumentException("Proxy is not a Vercel Relay protocol");
        }

        if (vercelToken == null || vercelToken.isBlank()) {
            throw new IllegalStateException("Vercel token not configured. Set vercel.token property.");
        }

        try {
            // Create relay deployment via Vercel API
            String sessionId = createRelaySession(proxy);
            activeSessions.put(proxy.getId(), sessionId);
            // Store sessionId in relaySessionId field for toProxyUrl() to use
            proxy.setRelaySessionId(sessionId);
            proxyRepository.save(proxy);

            log.info("Deployed Vercel Relay for proxy {} with session {}", proxy.getId(), sessionId);
            return sessionId;
        } catch (Exception e) {
            log.error("Failed to deploy Vercel Relay for proxy {}: {}", proxy.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to deploy Vercel Relay: " + e.getMessage(), e);
        }
    }

    /**
     * Create a relay session via Vercel API
     */
    private String createRelaySession(Proxy proxy) {
        String url = relayWorkerUrl + "/api/sessions";

        // Use proxy's vercelToken if available, otherwise use configured token
        String tokenToUse = proxy.getVercelToken() != null && !proxy.getVercelToken().isBlank()
                ? proxy.getVercelToken()
                : vercelToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + tokenToUse);

        Map<String, Object> request = new HashMap<>();
        request.put("target", proxy.getHost());
        request.put("port", proxy.getPort());
        request.put("proxyId", proxy.getId().toString());

        if (vercelTeamId != null && !vercelTeamId.isBlank()) {
            request.put("teamId", vercelTeamId);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.postForEntity(url, entity, Map.class).getBody();
            if (body != null) {
                return (String) body.get("sessionId");
            }
            throw new RuntimeException("Failed to create relay session: empty body");
        } catch (Exception e) {
            log.error("Error creating relay session: {}", e.getMessage());
            throw new RuntimeException("Failed to create relay session", e);
        }
    }

    /**
     * Get the SOCKS5 proxy URL for Vercel Relay
     */
    public String getRelayProxyUrl(UUID proxyId) {
        String sessionId = activeSessions.get(proxyId);
        if (sessionId == null) {
            throw new IllegalStateException("No active relay session for proxy " + proxyId);
        }
        return relayWorkerUrl + "/socks5?sessionId=" + sessionId;
    }

    /**
     * Stop and cleanup Vercel Relay session
     */
    public void stopRelay(UUID proxyId) {
        String sessionId = activeSessions.remove(proxyId);
        if (sessionId == null) {
            return;
        }

        try {
            String url = relayWorkerUrl + "/api/sessions/" + sessionId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + vercelToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Stopped Vercel Relay session {} for proxy {}", sessionId, proxyId);
        } catch (Exception e) {
            log.warn("Failed to stop relay session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Stop and cleanup Vercel Relay session using proxy's token
     */
    public void stopRelay(UUID proxyId, String proxyToken) {
        String sessionId = activeSessions.remove(proxyId);
        if (sessionId == null) {
            return;
        }

        try {
            String url = relayWorkerUrl + "/api/sessions/" + sessionId;
            HttpHeaders headers = new HttpHeaders();
            String tokenToUse = proxyToken != null && !proxyToken.isBlank() ? proxyToken : vercelToken;
            headers.set("Authorization", "Bearer " + tokenToUse);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Stopped Vercel Relay session {} for proxy {}", sessionId, proxyId);
        } catch (Exception e) {
            log.warn("Failed to stop relay session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Check if relay is active
     */
    public boolean isRelayActive(UUID proxyId) {
        return activeSessions.containsKey(proxyId);
    }

    /**
     * Get all active relay sessions
     */
    public Set<UUID> getActiveRelays() {
        return activeSessions.keySet();
    }

    /**
     * Cleanup all active relay sessions on application shutdown
     */
    @PreDestroy
    public void cleanupAllRelays() {
        log.info("Cleaning up {} active Vercel Relay sessions", activeSessions.size());
        activeSessions.keySet().forEach(this::stopRelay);
    }
}