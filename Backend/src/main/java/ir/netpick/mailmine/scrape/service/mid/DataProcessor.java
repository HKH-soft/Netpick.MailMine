package ir.netpick.mailmine.scrape.service.mid;

import ir.netpick.mailmine.scrape.model.ScrapeData;
import ir.netpick.mailmine.scrape.parser.ContactInfoParser;
import ir.netpick.mailmine.scrape.model.Contact;
import ir.netpick.mailmine.scrape.service.base.ContactService;
import ir.netpick.mailmine.scrape.service.base.FileManagement;
import ir.netpick.mailmine.scrape.service.base.ScrapeDataService;
import ir.netpick.mailmine.scrape.service.orch.PipelineControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessor {

    private final ContactService contactService;
    private final ScrapeDataService scrapeDataService;
    private final FileManagement fileManagement;
    private final PipelineControlService pipelineControlService;

    // Progress tracking
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private int totalCount = 0;

    private static final int BATCH_SIZE = 100; // Process in batches to avoid OOM

    public int getProcessedCount() {
        return processedCount.get();
    }

    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Process unparsed files in batches to avoid OOM with large datasets.
     * Each file is processed in its own transaction to prevent rollback of all
     * progress.
     */
    public void processUnparsedFiles() {
        // Get total count for progress tracking
        totalCount = (int) scrapeDataService.countUnparsed();
        processedCount.set(0);

        if (totalCount == 0) {
            log.info("No unparsed files found.");
            return;
        }

        log.info("Processing {} unparsed files in batches of {}", totalCount, BATCH_SIZE);

        int pageNum = 0;
        Page<ScrapeData> scrapeDataBatch;

        do {
            // Fetch a batch
            scrapeDataBatch = scrapeDataService.findUnparsedPaged(pageNum, BATCH_SIZE);

            for (ScrapeData scrapeData : scrapeDataBatch.getContent()) {
                // Check if pipeline is paused/cancelled/skipped
                try {
                    if (!pipelineControlService.checkAndWait()) {
                        log.info("Data processing stopped due to pipeline control (paused/cancelled/skipped)");
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Data processing interrupted");
                    return;
                }

                processSingleFile(scrapeData);
            }

            // Don't increment page number since we're marking items as processed
            // The next batch will automatically get the next unparsed items

        } while (scrapeDataBatch.hasNext() && !pipelineControlService.shouldStop());

        log.info("Finished processing. Total processed: {}/{}", processedCount.get(), totalCount);
    }

    /**
     * Process a single file in its own transaction.
     * This prevents rollback of all progress if one file fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleFile(ScrapeData scrapeData) {
        try {
            String htmlContent = fileManagement.readFile(
                    scrapeData.getScrapeJob().getId(),
                    scrapeData.getAttemptNumber(),
                    scrapeData.getFileName());

            // Handle null content - file not found or read error
            if (htmlContent == null) {
                log.error("Could not read file for ScrapeData ID: {} - marking as processed to skip",
                        scrapeData.getId());
                scrapeData.setParsed(true); // Mark as parsed to skip in future
                scrapeDataService.updateScrapeData(scrapeData);
                processedCount.incrementAndGet();
                return;
            }

            Contact parsedContact = ContactInfoParser.parse(htmlContent);
            if (parsedContact != null && parsedContact.hasContactInfo()) {
                // Link contact to scrape data for traceability
                parsedContact.setScrapeData(scrapeData);
                contactService.createContact(parsedContact);
                log.debug("Created contact with {} emails from ScrapeData ID: {}",
                        parsedContact.getEmails().size(), scrapeData.getId());
            } else {
                log.debug("No contact info found in ScrapeData ID: {}", scrapeData.getId());
            }

            scrapeData.setParsed(true);
            scrapeDataService.updateScrapeData(scrapeData);

            int processed = processedCount.incrementAndGet();
            if (processed % 50 == 0 || processed == totalCount) {
                log.info("[{}/{}] Processing progress...", processed, totalCount);
            }

        } catch (Exception e) {
            log.error("Error processing ScrapeData ID: {} - {}", scrapeData.getId(), e.getMessage());
            // Don't mark as parsed so it can be retried
            processedCount.incrementAndGet();
        }
    }
}
