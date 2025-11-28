package ir.netpick.mailmine.scrape.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ir.netpick.mailmine.scrape.dto.SearchQueryRequest;
import ir.netpick.mailmine.scrape.service.base.SearchQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scrape/search_queries")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class SearchQueryController {

    private final SearchQueryService searchQueryService;

    @GetMapping()
    public ResponseEntity<?> getSearchQueries(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(searchQueryService.allSearchQueries(page));
    }
    
    @GetMapping("/deleted")
    public ResponseEntity<?> getDeletedSearchQueries(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(searchQueryService.deletedSearchQueries(page));
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllSearchQueriesIncludingDeleted(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(searchQueryService.allSearchQueriesIncludingDeleted(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getSearchQuery(@PathVariable UUID id) {
        return ResponseEntity.ok().body(searchQueryService.getSearchQuery(id));
    }
    
    @GetMapping("/deleted/{id}")
    public ResponseEntity<?> getDeletedSearchQuery(@PathVariable UUID id) {
        return ResponseEntity.ok().body(searchQueryService.getSearchQueryIncludingDeleted(id));
    }

    @PostMapping()
    public ResponseEntity<?> createSearchQuery(@RequestBody SearchQueryRequest searchQuery) {
        return ResponseEntity.ok().body(searchQueryService.createSearchQuery(searchQuery));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateSearchQuery(@PathVariable UUID id, @RequestBody SearchQueryRequest searchQuery) {
        return ResponseEntity.ok().body(searchQueryService.updateSearchQuery(id, searchQuery));
    }
    
    @PutMapping("{id}/restore")
    public ResponseEntity<?> restoreSearchQuery(@PathVariable UUID id) {
        searchQueryService.restoreSearchQuery(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteSearchQuery(@PathVariable UUID id) {
        searchQueryService.softDeleteSearchQuery(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("{id}/full_delete")
    public ResponseEntity<?> fullDeleteSearchQuery(@PathVariable UUID id) {
        searchQueryService.deleteSearchQuery(id);
        return ResponseEntity.noContent().build();
    }
}