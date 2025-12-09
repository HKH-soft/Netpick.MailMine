package ir.netpick.mailmine.scrape.service.mid;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import ir.netpick.mailmine.scrape.ScrapeConstants;
import ir.netpick.mailmine.scrape.service.base.ScrapeJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ir.netpick.mailmine.scrape.model.ApiKey;
import ir.netpick.mailmine.scrape.model.LinkResult;
import ir.netpick.mailmine.scrape.model.SearchQuery;
import ir.netpick.mailmine.scrape.parser.LinkParser;
import ir.netpick.mailmine.scrape.repository.ApiKeyRepository;
import ir.netpick.mailmine.scrape.repository.SearchQueryRepository;
import ir.netpick.mailmine.scrape.service.orch.PipelineControlService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCaller {

    private final ApiKeyRepository apiKeyRepository;
    private final ScrapeJobService scrapeJobService;
    private final SearchQueryRepository searchQueryRepository;
    private final PipelineControlService pipelineControlService;

    // Configurable WebClient with timeouts (non-blocking)
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();

    @Value("${google.search.results-per-page:10}")
    private int resultsPerPage;

    @Value("${google.search.max-pages:3}")
    private int maxPages;

    @Value("${google.search.rate-limit-ms:1000}")
    private long rateLimitMs;

    @Value("${google.search.max-retries-per-page:3}")
    private int maxRetriesPerPage;

    @Value("${google.search.backoff-initial-ms:3000}")
    private long initialBackoffMs;

    @Value("${google.search.backoff-multiplier:2.0}")
    private double backoffMultiplier;

    @Value("${google.search.backoff-max-ms:20000}")
    private long maxBackoffMs;

    // Progress tracking
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private int totalCount = 0;

    public int getProcessedCount() {
        return processedCount.get();
    }

    public int getTotalCount() {
        return totalCount;
    }

    @Transactional
    public void callGoogleSearch() {
        List<ApiKey> apiKeys = apiKeyRepository.findAll();
        if (apiKeys.isEmpty()) {
            throw new RuntimeException("No API keys configured");
        }

        List<SearchQuery> queries = searchQueryRepository.findByLinkCountLessThan(ScrapeConstants.MAX_QUERY_COUNT);
        if (queries.isEmpty()) {
            log.info("No pending search queries found.");
            return;
        }

        // Initialize progress tracking
        processedCount.set(0);
        totalCount = queries.size();

        log.info("Processing {} search queries with rate limit of {}ms between API calls",
                totalCount, rateLimitMs);

        for (SearchQuery query : queries) {
            // Check if pipeline is paused/cancelled/skipped
            try {
                if (!pipelineControlService.checkAndWait()) {
                    log.info("API caller stopped due to pipeline control (paused/cancelled/skipped)");
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("API caller interrupted");
                break;
            }

            if (query.getSentence().isBlank()) {
                log.error("Query with id {} is blank", query.getId());
                processedCount.incrementAndGet();
                continue;
            }

            int linksCreated = processQuery(query, apiKeys);
            query.setLinkCount(query.getLinkCount() + linksCreated);
            searchQueryRepository.save(query);

            int processed = processedCount.incrementAndGet();
            log.info("[{}/{}] Query '{}' created {} links",
                    processed, totalCount,
                    truncate(query.getSentence(), 50), linksCreated);
        }
    }

    private int processQuery(SearchQuery query, List<ApiKey> apiKeys) {
        int totalLinksCreated = 0;
        int page = 0;
        int currentApiKeyIndex = ThreadLocalRandom.current().nextInt(apiKeys.size());
        long backoffDelay = initialBackoffMs;
        int retriesForPage = 0;

        while (page < maxPages) {
            if (pipelineControlService.shouldStop()) {
                log.info("Stopping query processing due to pipeline control");
                break;
            }

            if ((page > 0 || processedCount.get() > 0) && rateLimitMs > 0) {
                if (!sleepRespectingPipeline(rateLimitMs)) {
                    log.info("Rate limit wait interrupted or cancelled");
                    break;
                }
            }

            ApiKey currentKey = apiKeys.get(currentApiKeyIndex);
            String uri = buildUri(query.getSentence(), page, currentKey);

            try {
                String apiResponse = executeApiCall(uri).block();
                if (apiResponse == null || apiResponse.isEmpty()) {
                    log.warn("Empty response for query: {}", truncate(query.getSentence(), 50));
                    page++;
                    continue;
                }

                List<LinkResult> parsedLinks = LinkParser.parse(apiResponse);
                if (parsedLinks.isEmpty()) {
                    log.debug("No links parsed for query: {} (page {})",
                            truncate(query.getSentence(), 50), page);
                    page++;
                    currentApiKeyIndex = (currentApiKeyIndex + 1) % apiKeys.size();
                    continue;
                }

                List<String> urls = parsedLinks.stream().map(LinkResult::getLink).toList();
                List<String> titles = parsedLinks.stream().map(LinkResult::getTitle).toList();

                scrapeJobService.createJobsByList(urls, titles);
                totalLinksCreated += urls.size();
                log.debug("Created {} scrape jobs from query page {}", urls.size(), page);

                // Reset retry/backoff for next page and advance key for load balancing
                retriesForPage = 0;
                backoffDelay = initialBackoffMs;
                page++;
                currentApiKeyIndex = (currentApiKeyIndex + 1) % apiKeys.size();

            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429) {
                    retriesForPage++;
                    if (retriesForPage > maxRetriesPerPage) {
                        log.error(
                                "Exceeded retry limit ({} attempts) for query '{}' page {} due to repeated 429 responses.",
                                maxRetriesPerPage, truncate(query.getSentence(), 40), page);
                        break;
                    }

                    long waitMs = withJitter(backoffDelay);
                    log.warn("Rate limited (429) for query '{}' page {}. Waiting {} ms before retry (attempt {}/{}).",
                            truncate(query.getSentence(), 40), page, waitMs, retriesForPage, maxRetriesPerPage);

                    if (!sleepRespectingPipeline(waitMs)) {
                        log.info("Backoff wait interrupted or cancelled");
                        break;
                    }

                    backoffDelay = Math.min((long) (backoffDelay * backoffMultiplier), maxBackoffMs);
                    currentApiKeyIndex = (currentApiKeyIndex + 1) % apiKeys.size();
                    continue; // Retry same page
                }

                log.error("API call failed (HTTP {}): {} for query {} (page {})",
                        e.getStatusCode(), e.getMessage(),
                        truncate(query.getSentence(), 30), page);

                currentApiKeyIndex = (currentApiKeyIndex + 1) % apiKeys.size();
                if (currentApiKeyIndex == 0) {
                    log.error("All keys attempted for page {}. Aborting remaining pages.", page);
                    break;
                }

                if (!sleepRespectingPipeline(initialBackoffMs)) {
                    log.info("Backoff wait interrupted or cancelled after HTTP error");
                    break;
                }

            } catch (Exception e) {
                log.error("Unexpected error for query {} (page {}): {}",
                        truncate(query.getSentence(), 30), page, e.getMessage(), e);
                break;
            }
        }
        return totalLinksCreated;
    }

    private boolean sleepRespectingPipeline(long delayMs) {
        long remaining = delayMs;
        final long slice = 250L;
        while (remaining > 0) {
            if (pipelineControlService.shouldStop()) {
                return false;
            }
            long sleep = Math.min(slice, remaining);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            remaining -= sleep;
        }
        return true;
    }

    private long withJitter(long baseDelayMs) {
        long safeBase = Math.max(baseDelayMs, 1000L);
        long jitter = ThreadLocalRandom.current().nextLong(Math.max(1L, safeBase / 4));
        return safeBase + jitter;
    }

    private Mono<String> executeApiCall(String uri) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.debug("API call error: {}", e.getMessage()));
    }

    private String buildUri(String sentence, int page, ApiKey key) {
        String queryEncoded = Optional.ofNullable(sentence)
                .map(s -> s.replace(" ", "+"))
                .orElseThrow(() -> new IllegalArgumentException("Query sentence cannot be null"));

        int startIndex = page * resultsPerPage + 1;
        return key.getApiLink()
                .replace("<query>", queryEncoded)
                .replace("<api_key>", key.getKey())
                .replace("<search_engine_id>", key.getSearchEngineId())
                .replace("<start_index>", String.valueOf(startIndex))
                .replace("<count>", String.valueOf(resultsPerPage));
    }

    private String truncate(String str, int maxLen) {
        if (str == null)
            return "";
        return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }
}