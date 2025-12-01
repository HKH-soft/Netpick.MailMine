package ir.netpick.mailmine.scrape.service.mid;

import ir.netpick.mailmine.ai.service.GeminiService;
import ir.netpick.mailmine.scrape.dto.SearchQueryRequest;
import ir.netpick.mailmine.scrape.model.SearchQuery;
import ir.netpick.mailmine.scrape.service.base.SearchQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchQueryGenerator {

    private final GeminiService geminiService;
    private final SearchQueryService searchQueryService;

    private static final String SYSTEM_INSTRUCTION = """
            You are a search query generator for web scraping purposes.
            Your task is to generate effective search queries that can be used to find specific types of web pages.

            Rules:
            1. Generate unique, varied search queries
            2. Each query should be on a new line
            3. Do not include numbers or bullet points
            4. Make queries specific and targeted
            5. Do not include any explanations, just the queries
            """;

    /**
     * Generate search queries based on a topic and target description
     *
     * @param topic  The main topic or domain
     * @param target What type of pages/data to find
     * @param count  Number of queries to generate
     * @return List of generated query strings
     */
    public List<String> generateQueries(String topic, String target, int count) {
        String prompt = """
                Generate exactly %d search queries for web scraping.

                Topic: %s
                Target: %s

                Generate queries that would help find web pages containing %s related to %s.
                Return only the queries, one per line, no numbering.
                """.formatted(count, topic, target, target, topic);

        String fullPrompt = SYSTEM_INSTRUCTION + "\n\n" + prompt;

        try {
            String response = geminiService.generateText(fullPrompt);
            List<String> queries = parseQueries(response);
            log.info("Generated {} search queries for topic: {}", queries.size(), topic);
            return queries;
        } catch (Exception e) {
            log.error("Failed to generate queries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate search queries", e);
        }
    }

    /**
     * Generate search queries and save them directly to the database
     *
     * @param topic  The main topic or domain
     * @param target What type of pages/data to find
     * @param count  Number of queries to generate
     * @return List of saved SearchQuery entities
     */
    public List<SearchQuery> generateAndSaveQueries(String topic, String target, int count) {
        List<String> queries = generateQueries(topic, target, count);
        List<SearchQuery> savedQueries = new ArrayList<>();

        for (String query : queries) {
            try {
                SearchQueryRequest request = new SearchQueryRequest(
                        query,
                        0, // linkCount starts at 0
                        "Auto-generated for topic: " + topic);
                SearchQuery saved = searchQueryService.createSearchQuery(request);
                savedQueries.add(saved);
            } catch (Exception e) {
                log.warn("Failed to save query '{}': {}", query, e.getMessage());
                // Continue with other queries
            }
        }

        log.info("Saved {}/{} generated queries", savedQueries.size(), queries.size());
        return savedQueries;
    }

    /**
     * Generate variations of an existing query
     *
     * @param originalQuery The original search query
     * @param count         Number of variations to generate
     * @return List of query variations
     */
    public List<String> generateVariations(String originalQuery, int count) {
        String prompt = """
                Generate exactly %d variations of this search query.

                Original query: %s

                Create different phrasings and variations that would find similar results.
                Return only the queries, one per line, no numbering.
                """.formatted(count, originalQuery);

        String fullPrompt = SYSTEM_INSTRUCTION + "\n\n" + prompt;

        try {
            String response = geminiService.generateText(fullPrompt);
            List<String> variations = parseQueries(response);
            log.info("Generated {} variations for query: {}", variations.size(), originalQuery);
            return variations;
        } catch (Exception e) {
            log.error("Failed to generate variations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate query variations", e);
        }
    }

    /**
     * Generate queries with specific site restriction
     *
     * @param topic The main topic
     * @param site  The website domain to restrict to (e.g., "linkedin.com")
     * @param count Number of queries to generate
     * @return List of site-restricted queries
     */
    public List<String> generateSiteQueries(String topic, String site, int count) {
        String prompt = """
                Generate exactly %d search queries that include site: restriction.

                Topic: %s
                Target site: %s

                Each query should include "site:%s" and be designed to find relevant pages on that site.
                Return only the queries, one per line, no numbering.
                """.formatted(count, topic, site, site);

        String fullPrompt = SYSTEM_INSTRUCTION + "\n\n" + prompt;

        try {
            String response = geminiService.generateText(fullPrompt);
            List<String> queries = parseQueries(response);
            log.info("Generated {} site-restricted queries for: {}", queries.size(), site);
            return queries;
        } catch (Exception e) {
            log.error("Failed to generate site queries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate site-restricted queries", e);
        }
    }

    /**
     * Generate queries for finding email addresses
     *
     * @param industry The industry or business type
     * @param region   Geographic region (optional, can be null)
     * @param count    Number of queries to generate
     * @return List of email-finding queries
     */
    public List<String> generateEmailQueries(String industry, String region, int count) {
        String regionPart = region != null && !region.isBlank() ? " in " + region : "";

        String prompt = """
                Generate exactly %d search queries designed to find web pages that might contain email addresses.

                Industry: %s
                Region: %s

                Focus on finding contact pages, staff directories, about pages, or any pages likely to have email addresses.
                Use techniques like including "@" or "email" or "contact" in queries.
                Return only the queries, one per line, no numbering.
                """
                .formatted(count, industry, region != null ? region : "Global");

        String fullPrompt = SYSTEM_INSTRUCTION + "\n\n" + prompt;

        try {
            String response = geminiService.generateText(fullPrompt);
            List<String> queries = parseQueries(response);
            log.info("Generated {} email-finding queries for: {}{}", queries.size(), industry, regionPart);
            return queries;
        } catch (Exception e) {
            log.error("Failed to generate email queries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate email-finding queries", e);
        }
    }

    /**
     * Parse the AI response into individual queries
     */
    private List<String> parseQueries(String response) {
        if (response == null || response.isBlank()) {
            return List.of();
        }

        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.matches("^\\d+\\.?.*")) // Remove numbered lines
                .filter(line -> !line.startsWith("-")) // Remove bullet points
                .filter(line -> !line.startsWith("*")) // Remove asterisk bullets
                .filter(line -> line.length() > 3) // Remove too short lines
                .collect(Collectors.toList());
    }
}
