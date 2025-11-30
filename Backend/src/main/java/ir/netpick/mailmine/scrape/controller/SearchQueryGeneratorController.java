package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.dto.SearchQueryResponse;
import ir.netpick.mailmine.scrape.mapper.SearchQueryDTOMapper;
import ir.netpick.mailmine.scrape.model.SearchQuery;
import ir.netpick.mailmine.scrape.service.mid.SearchQueryGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search-queries/generate")
@RequiredArgsConstructor
public class SearchQueryGeneratorController {

    private final SearchQueryGenerator searchQueryGenerator;
    private final SearchQueryDTOMapper searchQueryDTOMapper;

    /**
     * Generate search queries based on topic and target
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> generateQueries(@RequestBody GenerateRequest request) {
        int count = request.count() != null ? request.count() : 10;
        List<String> queries = searchQueryGenerator.generateQueries(request.topic(), request.target(), count);
        return ResponseEntity.ok(Map.of(
                "queries", queries,
                "count", queries.size()));
    }

    /**
     * Generate and save search queries directly to database
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> generateAndSaveQueries(@RequestBody GenerateRequest request) {
        int count = request.count() != null ? request.count() : 10;
        List<SearchQuery> saved = searchQueryGenerator.generateAndSaveQueries(request.topic(), request.target(), count);
        List<SearchQueryResponse> responses = saved.stream().map(searchQueryDTOMapper).toList();
        return ResponseEntity.ok(Map.of(
                "queries", responses,
                "saved", saved.size()));
    }

    /**
     * Generate variations of an existing query
     */
    @PostMapping("/variations")
    public ResponseEntity<Map<String, Object>> generateVariations(@RequestBody VariationRequest request) {
        int count = request.count() != null ? request.count() : 5;
        List<String> variations = searchQueryGenerator.generateVariations(request.originalQuery(), count);
        return ResponseEntity.ok(Map.of(
                "original", request.originalQuery(),
                "variations", variations,
                "count", variations.size()));
    }

    /**
     * Generate site-restricted queries
     */
    @PostMapping("/site")
    public ResponseEntity<Map<String, Object>> generateSiteQueries(@RequestBody SiteQueryRequest request) {
        int count = request.count() != null ? request.count() : 10;
        List<String> queries = searchQueryGenerator.generateSiteQueries(request.topic(), request.site(), count);
        return ResponseEntity.ok(Map.of(
                "site", request.site(),
                "queries", queries,
                "count", queries.size()));
    }

    /**
     * Generate queries for finding email addresses
     */
    @PostMapping("/emails")
    public ResponseEntity<Map<String, Object>> generateEmailQueries(@RequestBody EmailQueryRequest request) {
        int count = request.count() != null ? request.count() : 10;
        List<String> queries = searchQueryGenerator.generateEmailQueries(request.industry(), request.region(), count);
        return ResponseEntity.ok(Map.of(
                "industry", request.industry(),
                "region", request.region() != null ? request.region() : "Global",
                "queries", queries,
                "count", queries.size()));
    }

    // Request DTOs
    public record GenerateRequest(String topic, String target, Integer count) {
    }

    public record VariationRequest(String originalQuery, Integer count) {
    }

    public record SiteQueryRequest(String topic, String site, Integer count) {
    }

    public record EmailQueryRequest(String industry, String region, Integer count) {
    }
}
