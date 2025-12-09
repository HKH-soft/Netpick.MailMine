package ir.netpick.mailmine.scrape.service.mid;

import ir.netpick.mailmine.scrape.model.Contact;
import ir.netpick.mailmine.scrape.model.ScrapeData;
import ir.netpick.mailmine.scrape.model.ScrapeJob;
import ir.netpick.mailmine.scrape.service.base.ContactService;
import ir.netpick.mailmine.scrape.service.base.FileManagement;
import ir.netpick.mailmine.scrape.service.base.ScrapeDataService;
import ir.netpick.mailmine.scrape.service.orch.PipelineControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataProcessor Tests")
class DataProcessorTest {

    @Mock
    private ContactService contactService;

    @Mock
    private ScrapeDataService scrapeDataService;

    @Mock
    private FileManagement fileManagement;

    @Mock
    private PipelineControlService pipelineControlService;

    @InjectMocks
    private DataProcessor dataProcessor;

    private ScrapeData scrapeData;
    private ScrapeJob scrapeJob;
    private UUID scrapeJobId;
    private UUID scrapeDataId;

    @BeforeEach
    void setUp() {
        scrapeJobId = UUID.randomUUID();
        scrapeDataId = UUID.randomUUID();

        scrapeJob = new ScrapeJob();
        scrapeJob.setId(scrapeJobId);

        scrapeData = new ScrapeData();
        scrapeData.setId(scrapeDataId);
        scrapeData.setScrapeJob(scrapeJob);
        scrapeData.setAttemptNumber(1);
        scrapeData.setFileName("test.html");
        scrapeData.setParsed(false);
    }

    @Nested
    @DisplayName("processUnparsedFiles() method")
    class ProcessUnparsedFilesMethod {

        @Test
        @DisplayName("should do nothing when no unparsed files exist")
        void shouldDoNothingWhenNoUnparsedFiles() {
            when(scrapeDataService.countUnparsed()).thenReturn(0L);

            dataProcessor.processUnparsedFiles();

            verify(scrapeDataService).countUnparsed();
            verifyNoInteractions(fileManagement);
            verifyNoInteractions(contactService);
        }

        @Test
        @DisplayName("should process files and create contact when email found")
        void shouldCreateContactWhenEmailFound() throws InterruptedException {
            String htmlWithEmail = "<html><body><p>Contact: test@example.com</p></body></html>";

            when(scrapeDataService.countUnparsed()).thenReturn(1L);
            when(scrapeDataService.findUnparsedPaged(0, 100))
                    .thenReturn(new PageImpl<>(List.of(scrapeData)));
            when(fileManagement.readFile(scrapeJobId, 1, "test.html")).thenReturn(htmlWithEmail);
            when(pipelineControlService.checkAndWait()).thenReturn(true);

            dataProcessor.processUnparsedFiles();

            verify(scrapeDataService).countUnparsed();
            verify(fileManagement).readFile(scrapeJobId, 1, "test.html");
            verify(contactService).createContact(any(Contact.class));
            verify(scrapeDataService).updateScrapeData(scrapeData);
            assertTrue(scrapeData.isParsed());
        }

        @Test
        @DisplayName("should NOT create contact when no email found")
        void shouldNotCreateContactWhenNoEmailFound() throws InterruptedException {
            String htmlWithoutEmail = "<html><body><p>No contact info here</p></body></html>";

            when(scrapeDataService.countUnparsed()).thenReturn(1L);
            when(scrapeDataService.findUnparsedPaged(0, 100))
                    .thenReturn(new PageImpl<>(List.of(scrapeData)));
            when(fileManagement.readFile(scrapeJobId, 1, "test.html")).thenReturn(htmlWithoutEmail);
            when(pipelineControlService.checkAndWait()).thenReturn(true);

            dataProcessor.processUnparsedFiles();

            verify(scrapeDataService).countUnparsed();
            verify(fileManagement).readFile(scrapeJobId, 1, "test.html");
            verify(contactService, never()).createContact(any(Contact.class));
            verify(scrapeDataService).updateScrapeData(scrapeData);
            assertTrue(scrapeData.isParsed());
        }

        @Test
        @DisplayName("should mark file as parsed even when no contact info")
        void shouldMarkFileAsParsedEvenWhenNoContactInfo() throws InterruptedException {
            String emptyHtml = "<html><body></body></html>";

            when(scrapeDataService.countUnparsed()).thenReturn(1L);
            when(scrapeDataService.findUnparsedPaged(0, 100))
                    .thenReturn(new PageImpl<>(List.of(scrapeData)));
            when(fileManagement.readFile(scrapeJobId, 1, "test.html")).thenReturn(emptyHtml);
            when(pipelineControlService.checkAndWait()).thenReturn(true);

            dataProcessor.processUnparsedFiles();

            assertTrue(scrapeData.isParsed());
            verify(scrapeDataService).updateScrapeData(scrapeData);
        }

        @Test
        @DisplayName("should handle null file content gracefully")
        void shouldHandleNullFileContentGracefully() throws InterruptedException {
            when(scrapeDataService.countUnparsed()).thenReturn(1L);
            when(scrapeDataService.findUnparsedPaged(0, 100))
                    .thenReturn(new PageImpl<>(List.of(scrapeData)));
            when(fileManagement.readFile(scrapeJobId, 1, "test.html")).thenReturn(null);
            when(pipelineControlService.checkAndWait()).thenReturn(true);

            // Should not throw exception
            assertDoesNotThrow(() -> dataProcessor.processUnparsedFiles());

            verify(contactService, never()).createContact(any(Contact.class));
            // File should be marked as parsed to skip in future
            assertTrue(scrapeData.isParsed());
        }

        @Test
        @DisplayName("should extract correct email from HTML content")
        void shouldExtractCorrectEmail() throws InterruptedException {
            String htmlWithEmail = "<html><body><a href='mailto:specific@domain.org'>Contact</a></body></html>";

            when(scrapeDataService.countUnparsed()).thenReturn(1L);
            when(scrapeDataService.findUnparsedPaged(0, 100))
                    .thenReturn(new PageImpl<>(List.of(scrapeData)));
            when(fileManagement.readFile(scrapeJobId, 1, "test.html")).thenReturn(htmlWithEmail);
            when(pipelineControlService.checkAndWait()).thenReturn(true);

            ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);

            dataProcessor.processUnparsedFiles();

            verify(contactService).createContact(contactCaptor.capture());
            Contact capturedContact = contactCaptor.getValue();
            assertTrue(capturedContact.getEmails().contains("specific@domain.org"));
        }

        @Test
        @DisplayName("should link contact to scrape data")
        void shouldLinkContactToScrapeData() throws InterruptedException {
            String htmlWithEmail = "<html><body>test@example.com</body></html>";

            when(scrapeDataService.countUnparsed()).thenReturn(1L);
            when(scrapeDataService.findUnparsedPaged(0, 100))
                    .thenReturn(new PageImpl<>(List.of(scrapeData)));
            when(fileManagement.readFile(scrapeJobId, 1, "test.html")).thenReturn(htmlWithEmail);
            when(pipelineControlService.checkAndWait()).thenReturn(true);

            ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);

            dataProcessor.processUnparsedFiles();

            verify(contactService).createContact(contactCaptor.capture());
            Contact capturedContact = contactCaptor.getValue();
            assertEquals(scrapeData, capturedContact.getScrapeData());
        }

        @Test
        @DisplayName("should stop when pipeline control signals stop")
        void shouldStopWhenPipelineControlSignalsStop() throws InterruptedException {
            when(scrapeDataService.countUnparsed()).thenReturn(1L);
            when(scrapeDataService.findUnparsedPaged(0, 100))
                    .thenReturn(new PageImpl<>(List.of(scrapeData)));
            when(pipelineControlService.checkAndWait()).thenReturn(false);

            dataProcessor.processUnparsedFiles();

            verify(fileManagement, never()).readFile(any(), anyInt(), any());
            verify(contactService, never()).createContact(any());
        }
    }
}
