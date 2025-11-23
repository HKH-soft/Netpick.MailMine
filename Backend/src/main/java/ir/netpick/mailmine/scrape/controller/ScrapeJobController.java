package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.base.ScrapeJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/scrape/scrape-jobs")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ScrapeJobController {
    private final ScrapeJobService scrapeJobService;

    @GetMapping
    public ResponseEntity<?> allJobs(@RequestParam(defaultValue = "1") int page){
        return ResponseEntity.ok()
                .body(scrapeJobService.allJobs(page));
    }

    @GetMapping
    @RequestMapping("{id}")
    public ResponseEntity<?> getJob(@PathVariable UUID id){
        return ResponseEntity.ok()
                .body(scrapeJobService.getScrapeJob(id));
    }
}
