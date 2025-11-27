package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.service.mid.DataProcessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.netpick.mailmine.scrape.service.mid.ApiCaller;
import ir.netpick.mailmine.scrape.service.mid.Scraper;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/scrape")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ScrapeController {

    private final Scraper scrape;
    private final ApiCaller apiCaller;
    private final DataProcessor dataProcessor;
    @PostMapping("start_google")
    public void startSearch() {
        apiCaller.callGoogleSearch();
    }

    @PostMapping("start_scrape")
    public void startScrapping() {
        scrape.scrapePendingJobs(true);
    }

    @PostMapping("start_extract")
    public void startExtract() {
        dataProcessor.processUnparsedFiles();
    }

}