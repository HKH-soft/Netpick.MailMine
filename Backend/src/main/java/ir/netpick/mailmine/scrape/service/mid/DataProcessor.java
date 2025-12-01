package ir.netpick.mailmine.scrape.service.mid;

import ir.netpick.mailmine.scrape.model.ScrapeData;
import ir.netpick.mailmine.scrape.parser.ContactInfoParser;
import ir.netpick.mailmine.scrape.model.Contact;
import ir.netpick.mailmine.scrape.file.FileManagement;
import ir.netpick.mailmine.scrape.service.base.ContactService;
import ir.netpick.mailmine.scrape.service.base.ScrapeDataService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessor {

    private final ContactService contactService;
    private final ScrapeDataService scrapeDataService;
    private final FileManagement fileManagement;

    @Transactional
    public void processUnparsedFiles() {
        List<ScrapeData> unparsedFiles = scrapeDataService.findUnparsed();

        if (unparsedFiles.isEmpty()) {
            log.info("No unparsed files found.");
            return;
        }

        for (ScrapeData scrapeData : unparsedFiles) {
            processSingleFile(scrapeData);
        }
    }

    private void processSingleFile(ScrapeData scrapeData) {
        try {

            String content = fileManagement.ReadAFile(scrapeData.getScrapeJob().getId(),
                    scrapeData.getAttemptNumber(),
                    scrapeData.getFileName());

            Contact parsedContact = ContactInfoParser.parse(content);
            if (parsedContact != null && parsedContact.hasContactInfo()) {
                contactService.createContact(parsedContact);
            } else {
                log.info("No contact info found in ScrapeData ID: {} - skipping creation", scrapeData.getId());
            }

            scrapeData.setParsed(true);

            scrapeDataService.updateScrapeData(scrapeData);
            log.info("Successfully parsed and updated ScrapeData ID: {}", scrapeData.getId());
        } catch (Exception e) {
            log.error("Unexpected error while processing ScrapeData ID: {}", scrapeData.getId(), e);
        }
    }
}
