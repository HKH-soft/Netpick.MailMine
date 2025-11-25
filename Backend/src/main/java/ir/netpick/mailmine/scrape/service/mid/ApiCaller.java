package ir.netpick.mailmine.scrape.service.mid;

import java.util.List;
import java.util.Optional;

import ir.netpick.mailmine.scrape.ScrapeConstants;
import ir.netpick.mailmine.scrape.service.base.ScrapeJobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ir.netpick.mailmine.scrape.model.ApiKey;
import ir.netpick.mailmine.scrape.model.LinkResult;
import ir.netpick.mailmine.scrape.model.SearchQuery;
import ir.netpick.mailmine.scrape.parser.LinkParser;
import ir.netpick.mailmine.scrape.repository.ApiKeyRepository;
import ir.netpick.mailmine.scrape.repository.SearchQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApiCaller {

    private final ApiKeyRepository apiKeyRepository;
    private final ScrapeJobService scrapeJobService;
    private final SearchQueryRepository searchQueryRepository;

    // Configurable WebClient with timeouts (non-blocking)
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // Increase buffer if
                                                                                                // needed
            .build();

    @Value("${google.search.results-per-page:10}")
    private int resultsPerPage;

    @Value("${google.search.max-pages:3}")
    private int maxPages;

    @Async
    @Transactional
    public void callGoogleSearch() {
        List<ApiKey> keys = apiKeyRepository.findAll();
        if (keys.isEmpty()) {
            throw new RuntimeException("No API keys configured");
        }

        List<SearchQuery> queries = searchQueryRepository.findByLink_countLessThan(ScrapeConstants.MAX_QUERY_COUNT);
        if (queries.isEmpty()) {
            log.info("No pending search queries found.");
            return;
        }

        for (SearchQuery query : queries) {
            if (query.getSentence().isBlank()) {
                log.error("Query with id {} is blank", query.getId());
                continue;
            }
            processQuery(query, keys);
        }
    }

    private void processQuery(SearchQuery query, List<ApiKey> keys) {
        int keyIndex = 0;
        for (int page = 0; page < maxPages; page++) {
            ApiKey currentKey = keys.get(keyIndex);

            String uri = buildUri(query.getSentence(), page, currentKey);

            try {
                String json = executeApiCall(uri).block();
                if (json == null || json.isEmpty()) {
                    log.warn("Empty response for query: {}", query.getSentence());
                    continue;
                }

                List<LinkResult> parsedLinks = LinkParser.parse(json);
                if (parsedLinks.isEmpty()) {
                    log.info("No links parsed for query: {} (page {})", query.getSentence(), page);
                    continue;
                }

                List<String> urls = parsedLinks.stream().map(LinkResult::getLink).toList();
                List<String> titles = parsedLinks.stream().map(LinkResult::getTitle).toList();

                scrapeJobService.createJobList(urls, titles);
                log.info("Created scrape jobs for {} links from query: {} (page {})", urls.size(),
                        query.getSentence(), page);

            } catch (WebClientResponseException e) {
                log.error("API call failed (HTTP {}): {} for query {} (page {})", e.getStatusCode(), e.getMessage(),
                        query.getSentence(), page, e);
                keyIndex = (keyIndex + 1) % keys.size(); // Rotate key
                if (keyIndex == 0) {
                    log.error("All keys failed for page {}; skipping.", page);
                    break; // All keys tried, skip remaining pages
                }
                page--; // Retry same page with next key
            } catch (Exception e) {
                log.error("Unexpected error for query {} (page {}): {}", query.getSentence(), page, e.getMessage(),
                        e);
                break;
            }
        }
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
}