package ir.netpick.mailmine.scrape.service.orch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapeScheduleService {

    private final ScrapeOrchestrationService orchestrationService;

    @Scheduled(cron = "0 0 */3 * * *")
    public void scheduledScrapeJob() {
        log.info("===== Starting scheduled scraping pipeline =====");
        // Execute all steps through orchestration (runs async)
        orchestrationService.executeAllSteps();
        log.info("===== Scraping pipeline started (running in background) =====");
    }
}
