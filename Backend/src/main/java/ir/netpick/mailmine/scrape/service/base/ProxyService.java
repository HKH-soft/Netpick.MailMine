package ir.netpick.mailmine.scrape.service.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import ir.netpick.mailmine.common.enums.ProxyStatus;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.model.Proxy;
import ir.netpick.mailmine.scrape.repository.ProxyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyService {

    private final ProxyRepository proxyRepository;
    private final V2RayClientService v2RayClientService;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    private static final String TEST_URL = "https://httpbin.org/ip";
    private static final int TEST_TIMEOUT_MS = 15000;
    private static final int SLOW_THRESHOLD_MS = 5000;

    // ==================== CRUD Operations ====================

    public PageDTO<Proxy> allProxies(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Proxy> page = proxyRepository.findByDeletedFalse(pageable);
        return new PageDTO<>(page.getContent(), page.getTotalPages(), pageNumber);
    }

    public PageDTO<Proxy> deletedProxies(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Proxy> page = proxyRepository.findByDeletedTrue(pageable);
        return new PageDTO<>(page.getContent(), page.getTotalPages(), pageNumber);
    }

    public Proxy getProxy(UUID id) {
        return proxyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proxy with ID [%s] not found.".formatted(id)));
    }

    public Proxy createProxy(Proxy proxy) {
        if (proxyRepository.existsByHostAndPort(proxy.getHost(), proxy.getPort())) {
            log.warn("Proxy {}:{} already exists, skipping", proxy.getHost(), proxy.getPort());
            return proxyRepository.findByHostAndPort(proxy.getHost(), proxy.getPort()).orElse(proxy);
        }
        Proxy saved = proxyRepository.save(proxy);
        log.info("Created proxy: {}:{}", saved.getHost(), saved.getPort());
        return saved;
    }

    public Proxy updateProxy(UUID id, Proxy updates) {
        Proxy existing = getProxy(id);

        if (updates.getHost() != null)
            existing.setHost(updates.getHost());
        if (updates.getPort() != null)
            existing.setPort(updates.getPort());
        if (updates.getProtocol() != null)
            existing.setProtocol(updates.getProtocol());
        if (updates.getUsername() != null)
            existing.setUsername(updates.getUsername());
        if (updates.getPassword() != null)
            existing.setPassword(updates.getPassword());
        if (updates.getStatus() != null)
            existing.setStatus(updates.getStatus());
        if (updates.getDescription() != null)
            existing.setDescription(updates.getDescription());

        return proxyRepository.save(existing);
    }

    public void softDelete(UUID id) {
        proxyRepository.softDelete(id);
        log.info("Soft deleted proxy with ID: {}", id);
    }

    public void restore(UUID id) {
        proxyRepository.restore(id);
        log.info("Restored proxy with ID: {}", id);
    }

    public void deleteProxy(UUID id) {
        proxyRepository.deleteById(id);
        log.info("Permanently deleted proxy with ID: {}", id);
    }

    // ==================== Import from File ====================

    /**
     * Import proxies from a file. Supports formats:
     * - One proxy per line
     * - socks5://host:port
     * - host:port (defaults to SOCKS5)
     * - socks5://user:pass@host:port
     */
    public int importFromFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            return importProxyLines(lines);
        } catch (Exception e) {
            log.error("Failed to read proxy file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read proxy file: " + e.getMessage());
        }
    }

    /**
     * Import proxies from uploaded MultipartFile
     */
    public int importFromMultipartFile(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String> lines = reader.lines().toList();
            return importProxyLines(lines);
        } catch (Exception e) {
            log.error("Failed to read uploaded proxy file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read uploaded file: " + e.getMessage());
        }
    }

    private int importProxyLines(List<String> lines) {
        int imported = 0;
        int skipped = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue; // Skip empty lines and comments
            }

            try {
                Proxy proxy = Proxy.fromUrl(trimmed);
                if (!proxyRepository.existsByHostAndPort(proxy.getHost(), proxy.getPort())) {
                    proxyRepository.save(proxy);
                    imported++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                log.warn("Failed to parse proxy line: {} - {}", trimmed, e.getMessage());
            }
        }

        log.info("Imported {} proxies, skipped {} duplicates", imported, skipped);
        return imported;
    }

    // ==================== Proxy Testing ====================

    /**
     * Test a single proxy and update its status
     */
    public Proxy testProxy(UUID proxyId) {
        Proxy proxy = getProxy(proxyId);
        return testAndUpdateProxy(proxy);
    }

    /**
     * Test all untested proxies
     */
    @Async
    public CompletableFuture<Integer> testUntestedProxies() {
        List<Proxy> untested = proxyRepository.findUntestedProxies();
        log.info("Testing {} untested proxies...", untested.size());

        int tested = 0;
        for (Proxy proxy : untested) {
            testAndUpdateProxy(proxy);
            tested++;
        }

        log.info("Finished testing {} proxies", tested);
        return CompletableFuture.completedFuture(tested);
    }

    /**
     * Test all active proxies to verify they still work
     */
    @Async
    public CompletableFuture<Integer> testActiveProxies() {
        List<Proxy> active = proxyRepository.findByStatusAndDeletedFalse(ProxyStatus.ACTIVE);
        log.info("Re-testing {} active proxies...", active.size());

        int tested = 0;
        for (Proxy proxy : active) {
            testAndUpdateProxy(proxy);
            tested++;
        }

        log.info("Finished re-testing {} proxies", tested);
        return CompletableFuture.completedFuture(tested);
    }

    private Proxy testAndUpdateProxy(Proxy proxy) {
        log.debug("Testing proxy: {}", proxy.toProxyUrl());

        // For V2Ray proxies, start the client first
        if (proxy.isV2RayProtocol()) {
            try {
                v2RayClientService.startProxy(proxy);
            } catch (Exception e) {
                log.error("Failed to start V2Ray client for proxy {}: {}", proxy.getId(), e.getMessage());
                proxy.setStatus(ProxyStatus.FAILED);
                proxy.recordFailure();
                proxy.setLastTestedAt(LocalDateTime.now());
                return proxyRepository.save(proxy);
            }
        }

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setProxy(new com.microsoft.playwright.options.Proxy(proxy.toProxyUrl()));

            long startTime = System.currentTimeMillis();

            try (Browser browser = playwright.chromium().launch(launchOptions)) {
                try (BrowserContext context = browser.newContext()) {
                    com.microsoft.playwright.Page page = context.newPage();
                    page.setDefaultTimeout(TEST_TIMEOUT_MS);

                    page.navigate(TEST_URL);
                    String content = page.content();

                    long responseTime = System.currentTimeMillis() - startTime;

                    if (content.contains("origin")) {
                        proxy.setStatus(responseTime > SLOW_THRESHOLD_MS ? ProxyStatus.SLOW : ProxyStatus.ACTIVE);
                        proxy.recordSuccess(responseTime);
                        log.info("Proxy {} is {} ({}ms)", proxy.toProxyUrl(), proxy.getStatus(), responseTime);
                    } else {
                        proxy.setStatus(ProxyStatus.FAILED);
                        proxy.recordFailure();
                        log.warn("Proxy {} returned unexpected response", proxy.toProxyUrl());
                    }
                }
            }
        } catch (Exception e) {
            proxy.setStatus(ProxyStatus.FAILED);
            proxy.recordFailure();
            log.warn("Proxy {} failed: {}", proxy.toProxyUrl(), e.getMessage());
        } finally {
            // Stop V2Ray client after testing
            if (proxy.isV2RayProtocol()) {
                v2RayClientService.stopProxy(proxy.getId());
            }
        }

        proxy.setLastTestedAt(LocalDateTime.now());
        return proxyRepository.save(proxy);
    }

    // ==================== Proxy Selection for Scraping ====================

    /**
     * Get the next available proxy using round-robin selection
     */
    public Optional<Proxy> getNextProxy() {
        List<Proxy> activeProxies = proxyRepository.findByStatusInAndDeletedFalse(
                List.of(ProxyStatus.ACTIVE, ProxyStatus.SLOW));

        if (activeProxies.isEmpty()) {
            log.warn("No active proxies available");
            return Optional.empty();
        }

        int index = roundRobinIndex.getAndIncrement() % activeProxies.size();
        Proxy proxy = activeProxies.get(index);
        log.debug("Selected proxy: {}", proxy.toProxyUrl());
        return Optional.of(proxy);
    }

    /**
     * Get the best proxy based on response time and success rate
     */
    public Optional<Proxy> getBestProxy() {
        List<Proxy> bestProxies = proxyRepository.findBestProxies(
                List.of(ProxyStatus.ACTIVE, ProxyStatus.SLOW));

        if (bestProxies.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(bestProxies.get(0));
    }

    /**
     * Get a random active proxy
     */
    public Optional<Proxy> getRandomProxy() {
        List<Proxy> activeProxies = proxyRepository.findByStatusInAndDeletedFalse(
                List.of(ProxyStatus.ACTIVE, ProxyStatus.SLOW));

        if (activeProxies.isEmpty()) {
            return Optional.empty();
        }

        Proxy proxy = activeProxies.get(new Random().nextInt(activeProxies.size()));
        return Optional.of(proxy);
    }

    /**
     * Record successful use of a proxy
     */
    public void recordProxySuccess(UUID proxyId, long responseTimeMs) {
        Proxy proxy = getProxy(proxyId);
        proxy.recordSuccess(responseTimeMs);
        proxyRepository.save(proxy);
    }

    /**
     * Record failed use of a proxy
     */
    public void recordProxyFailure(UUID proxyId) {
        Proxy proxy = getProxy(proxyId);
        proxy.recordFailure();

        // Disable proxy if too many failures
        if (proxy.getFailureCount() > 5 && proxy.getSuccessCount() < proxy.getFailureCount()) {
            proxy.setStatus(ProxyStatus.FAILED);
            log.warn("Proxy {} disabled due to too many failures", proxy.toProxyUrl());
        }

        proxyRepository.save(proxy);
    }

    // ==================== Stats ====================

    public Map<String, Long> getProxyStats() {
        Map<String, Long> stats = new HashMap<>();
        for (ProxyStatus status : ProxyStatus.values()) {
            stats.put(status.name(), proxyRepository.countByStatusAndDeletedFalse(status));
        }
        return stats;
    }
}
