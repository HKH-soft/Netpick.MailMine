package ir.netpick.mailmine.scrape.service.mid;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.playwright.Browser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.Proxy;
import ir.netpick.mailmine.scrape.service.base.ProxyService;
import ir.netpick.mailmine.scrape.service.base.ScrapeDataService;
import ir.netpick.mailmine.scrape.service.base.ScrapeJobService;
import ir.netpick.mailmine.scrape.service.base.V2RayClientService;
import ir.netpick.mailmine.scrape.service.orch.PipelineControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ir.netpick.mailmine.scrape.ScrapeConstants;
import ir.netpick.mailmine.scrape.model.ScrapeJob;
import ir.netpick.mailmine.scrape.repository.ScrapeJobRepository;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class Scraper {

    private final ScrapeJobRepository scrapeJobRepository;
    private final ScrapeJobService scrapeJobService;
    private final ScrapeDataService scrapeDataService;
    private final ProxyService proxyService;
    private final V2RayClientService v2RayClientService;
    private final PipelineControlService pipelineControlService;

    @Value("${scraper.use-proxy:true}")
    private boolean useProxy;

    @Value("${scraper.batch-size:100}")
    private int scraperBatchSize;

    // Progress tracking
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private int totalCount = 0;

    public int getDataCount() {
        return (int) scrapeDataService.countAll();
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void scrapePendingJobs() {
        scrapePendingJobs(true);
    }

    public void scrapePendingJobs(boolean headless) {
        long pendingJobs = scrapeJobRepository.countPendingJobs(ScrapeConstants.MAX_ATTEMPTS);
        if (pendingJobs == 0) {
            log.info("No Scrape jobs left to process");
            return;
        }

        processedCount.set(0);
        totalCount = (int) pendingJobs;

        log.info("Starting to scrape {} pending jobs (useProxy={}, batchSize={})",
                totalCount, useProxy, scraperBatchSize);

        try (Playwright playwright = Playwright.create()) {
            while (true) {
                List<ScrapeJob> scrapeJobs = fetchPendingJobs();
                if (scrapeJobs.isEmpty()) {
                    log.debug("No more pending jobs found in current batch");
                    break;
                }

                for (ScrapeJob scrapeJob : scrapeJobs) {
                    try {
                        if (!pipelineControlService.checkAndWait()) {
                            log.info("Scraping stopped due to pipeline control (paused/cancelled/skipped)");
                            return;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.info("Scraping interrupted");
                        return;
                    }

                    processJobWithProxy(scrapeJob, playwright, headless);
                }
            }
        } catch (PlaywrightException e) {
            log.error("Failed to initialize Playwright: {}", e.getMessage(), e);
        }
    }

    private List<ScrapeJob> fetchPendingJobs() {
        PageRequest pageRequest = PageRequest.of(0, scraperBatchSize, Sort.by("createdAt").ascending());
        return scrapeJobRepository.findPendingJobs(ScrapeConstants.MAX_ATTEMPTS, pageRequest)
                .stream()
                .filter(job -> !isBlockedDomain(job.getLink()))
                .toList();
    }

    /**
     * Check if a URL belongs to a blocked domain
     */
    private boolean isBlockedDomain(String url) {
        try {
            String host = new URI(url).getHost();
            if (host == null)
                return false;
            return Arrays.stream(ScrapeConstants.BLOCKED_DOMAINS)
                    .anyMatch(blocked -> host.contains(blocked));
        } catch (Exception e) {
            log.warn("Failed to parse URL for domain check: {}", url);
            return false;
        }
    }

    private void processJobWithProxy(ScrapeJob scrapeJob, Playwright playwright, boolean headless) {
        // Get a proxy for this job
        Optional<ir.netpick.mailmine.scrape.model.Proxy> proxyOpt = useProxy ? proxyService.getNextProxy()
                : Optional.empty();

        // Start V2Ray client if needed
        ir.netpick.mailmine.scrape.model.Proxy proxyModel = null;
        if (proxyOpt.isPresent()) {
            proxyModel = proxyOpt.get();
            if (proxyModel.isV2RayProtocol()) {
                try {
                    v2RayClientService.startProxy(proxyModel);
                } catch (Exception e) {
                    log.error("Failed to start V2Ray client for proxy {}: {}", proxyModel.getId(), e.getMessage());
                    proxyOpt = Optional.empty(); // Fall back to no proxy
                }
            }
        }

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(List.of(
                        "--disable-gpu",
                        "--no-sandbox",
                        "--disable-dev-shm-usage"));

        // Apply proxy if available
        if (proxyOpt.isPresent() && proxyModel != null) {
            launchOptions.setProxy(new Proxy(proxyModel.toProxyUrl()));
            log.debug("Using proxy: {}", proxyModel.toProxyUrl());
        } else if (useProxy) {
            log.warn("No active proxy available, scraping without proxy");
        }

        long startTime = System.currentTimeMillis();
        final ir.netpick.mailmine.scrape.model.Proxy finalProxyModel = proxyModel;

        try (Browser browser = playwright.chromium().launch(launchOptions)) {
            try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(true))) {

                Page page = context.newPage();
                page.setDefaultTimeout(ScrapeConstants.PAGE_LOAD_TIMEOUT_SECONDS * 1000);

                // Navigate to URL
                page.navigate(scrapeJob.getLink());

                // Wait for body
                page.waitForSelector("body", new Page.WaitForSelectorOptions()
                        .setTimeout(ScrapeConstants.PAGE_LOAD_TIMEOUT_SECONDS * 1000));

                // Get page content
                String pageSource = page.content();
                scrapeDataService.createScrapeData(pageSource, scrapeJob.getId());

                // Record success - MARK AS SCRAPED!
                scrapeJob.setAttempt(scrapeJob.getAttempt() + 1);
                scrapeJob.setBeenScraped(true); // Critical fix: mark as scraped
                scrapeJob.setScrapeFailed(false);
                scrapeJobService.updateScrapeJob(scrapeJob.getId(), scrapeJob);

                // Record proxy success
                if (proxyOpt.isPresent()) {
                    long responseTime = System.currentTimeMillis() - startTime;
                    proxyService.recordProxySuccess(proxyOpt.get().getId(), responseTime);
                }

                // Update progress
                int processed = processedCount.incrementAndGet();
                log.info("[{}/{}] Successfully scraped: {}", processed, totalCount, scrapeJob.getLink());
            }
        } catch (PlaywrightException e) {
            handleScrapeFailure(scrapeJob, proxyOpt, e);
        } catch (Exception e) {
            handleScrapeFailure(scrapeJob, proxyOpt, e);
        } finally {
            // Stop V2Ray client if it was started
            if (finalProxyModel != null && finalProxyModel.isV2RayProtocol()) {
                v2RayClientService.stopProxy(finalProxyModel.getId());
            }
        }
    }

    private void handleScrapeFailure(ScrapeJob scrapeJob,
            Optional<ir.netpick.mailmine.scrape.model.Proxy> proxyOpt,
            Exception e) {
        // Increment attempt count on failure
        scrapeJob.setAttempt(scrapeJob.getAttempt() + 1);

        // Only mark as permanently failed if max attempts reached
        if (scrapeJob.getAttempt() >= ScrapeConstants.MAX_ATTEMPTS) {
            scrapeJob.setScrapeFailed(true);
            log.error("[FAILED] Max attempts reached for job {}: {}", scrapeJob.getId(), scrapeJob.getLink());
        } else {
            log.warn("[RETRY {}/{}] Failed to scrape {}: {}",
                    scrapeJob.getAttempt(), ScrapeConstants.MAX_ATTEMPTS,
                    scrapeJob.getLink(), e.getMessage());
        }

        scrapeJobService.updateScrapeJob(scrapeJob.getId(), scrapeJob);

        // Record proxy failure
        if (proxyOpt.isPresent()) {
            proxyService.recordProxyFailure(proxyOpt.get().getId());
        }

        // Update progress
        processedCount.incrementAndGet();
    }
}