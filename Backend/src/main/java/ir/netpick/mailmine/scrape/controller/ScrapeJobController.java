package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.base.ScrapeJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/scrape/scrape_jobs")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ScrapeJobController {
    private final ScrapeJobService scrapeJobService;

    @GetMapping
    public ResponseEntity<?> allJobs(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(scrapeJobService.allJobs(page));
    }

    @GetMapping("/deleted")
    public ResponseEntity<?> deletedJobs(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(scrapeJobService.deletedJobs(page));
    }

    @GetMapping("/all")
    public ResponseEntity<?> allJobsIncludingDeleted(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok()
                .body(scrapeJobService.allJobsIncludingDeleted(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getJob(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .body(scrapeJobService.getJob(id));
    }

    @GetMapping("/deleted/{id}")
    public ResponseEntity<?> getDeletedJob(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .body(scrapeJobService.getJobIncludingDeleted(id));
    }

    @PutMapping("{id}/restore")
    public ResponseEntity<?> restoreJob(@PathVariable UUID id) {
        scrapeJobService.restore(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> softDeleteJob(@PathVariable UUID id) {
        scrapeJobService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("{id}/full_delete")
    public ResponseEntity<?> fullDeleteJob(@PathVariable UUID id) {
        scrapeJobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(scrapeJobService.getStats());
    }
}