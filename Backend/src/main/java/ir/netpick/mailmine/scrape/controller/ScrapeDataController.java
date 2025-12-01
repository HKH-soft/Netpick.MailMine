package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.base.ScrapeDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/scrape/scrape_data")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ScrapeDataController {
    private final ScrapeDataService scrapeDataService;

    @GetMapping
    public ResponseEntity<?> allScrapeData(@RequestParam(defaultValue = "1") int page){
        return ResponseEntity.ok()
                .body(scrapeDataService.allData(page));
    }
    
    @GetMapping("/deleted")
    public ResponseEntity<?> deletedScrapeData(@RequestParam(defaultValue = "1") int page){
        return ResponseEntity.ok()
                .body(scrapeDataService.deletedData(page));
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> allScrapeDataIncludingDeleted(@RequestParam(defaultValue = "1") int page){
        return ResponseEntity.ok()
                .body(scrapeDataService.allDataIncludingDeleted(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getScrapeData(@PathVariable UUID id){
        return ResponseEntity.ok()
                .body(scrapeDataService.getData(id));
    }
    
    @GetMapping("/deleted/{id}")
    public ResponseEntity<?> getDeletedScrapeData(@PathVariable UUID id){
        return ResponseEntity.ok()
                .body(scrapeDataService.getDataIncludingDeleted(id));
    }
    
    @PutMapping("{id}/restore")
    public ResponseEntity<?> restoreScrapeData(@PathVariable UUID id) {
        scrapeDataService.restoreData(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("{id}")
    public ResponseEntity<?> softDeleteScrapeData(@PathVariable UUID id) {
        scrapeDataService.softDeleteData(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("{id}/full_delete")
    public ResponseEntity<?> fullDeleteScrapeData(@PathVariable UUID id) {
        scrapeDataService.deleteData(id);
        return ResponseEntity.noContent().build();
    }
}