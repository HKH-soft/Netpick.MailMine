package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.base.ScrapeDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/scrape/scrape-data")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ScrapeDataController {
    private final ScrapeDataService scrapeDataService;

    @GetMapping
    public ResponseEntity<?> allScrapeData(@RequestParam(defaultValue = "1") int page){
        return ResponseEntity.ok()
                .body(scrapeDataService.allData(page));
    }

    @GetMapping
    @RequestMapping("{id}")
    public ResponseEntity<?> getScrapeData(@PathVariable UUID id){
        return ResponseEntity.ok()
                .body(scrapeDataService.getData(id));
    }
}
