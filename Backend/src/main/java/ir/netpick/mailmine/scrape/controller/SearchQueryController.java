package ir.netpick.mailmine.scrape.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.netpick.mailmine.scrape.dto.SearchQueryRequest;
import ir.netpick.mailmine.scrape.service.base.SearchQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class SearchQueryController {

    private final SearchQueryService searchQueryService;

    @GetMapping("/search_query")
    public ResponseEntity<?> getSearchQuerys(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(searchQueryService.allSearchQueries(page));
    }

    @GetMapping("/search_query/{id}")
    public ResponseEntity<?> getSearchQuery(@PathVariable UUID id) {
        return ResponseEntity.ok().body(searchQueryService.getSearchQuery(id));
    }

    @PostMapping("/search_query")
    public ResponseEntity<?> createSearchQuery(@RequestBody SearchQueryRequest searchQuery) {
        return ResponseEntity.ok().body(searchQueryService.createSearchQuery(searchQuery));
    }

    @PutMapping("/search_query/{id}")
    public ResponseEntity<?> updateSearchQuery(@PathVariable UUID id, @RequestBody SearchQueryRequest searchQuery) {
        return ResponseEntity.ok().body(searchQueryService.updateSearchQuery(id, searchQuery));
    }

    @DeleteMapping("/search_query/{id}")
    public ResponseEntity<?> deleteSearchQuery(@PathVariable UUID id) {
        searchQueryService.deleteSearchQuery(id);
        return ResponseEntity.noContent().build();
    }
}
