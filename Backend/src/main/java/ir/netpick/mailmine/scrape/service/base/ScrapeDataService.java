package ir.netpick.mailmine.scrape.service.base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.model.ScrapeData;
import ir.netpick.mailmine.scrape.model.ScrapeJob;
import ir.netpick.mailmine.scrape.repository.ScrapeDataRepository;
import ir.netpick.mailmine.scrape.repository.ScrapeJobRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScrapeDataService {

    private final ScrapeDataRepository scrapeDataRepository;
    private final ScrapeJobRepository scrapeJobRepository;
    private final FileManagement fileManagement;

    public boolean isEmpty() {
        return scrapeDataRepository.count() == 0;
    }

    /**
     * @deprecated Use findUnparsedPaged instead to avoid OOM with large datasets
     */
    @Deprecated
    public List<ScrapeData> findUnparsed() {
        return scrapeDataRepository.findByParsedFalse();
    }

    /**
     * Find unparsed files with pagination
     */
    public Page<ScrapeData> findUnparsedPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return scrapeDataRepository.findByParsedFalseAndDeletedFalse(pageable);
    }

    /**
     * Count unparsed files for progress tracking
     */
    public long countUnparsed() {
        return scrapeDataRepository.countByParsedFalseAndDeletedFalse();
    }

    public List<ScrapeData> allData() {
        return scrapeDataRepository.findAll();
    }

    public long countAll() {
        return scrapeDataRepository.count();
    }

    public PageDTO<ScrapeData> allData(int page) {
        Pageable pageable = PageRequest.of(page - 1, GeneralConstants.PAGE_SIZE, Sort.by("createdAt").descending());
        Page<ScrapeData> pageContent = scrapeDataRepository.findByDeletedFalse(pageable);
        return new PageDTO<>(
                pageContent.getContent(),
                pageContent.getTotalPages(),
                page);
    }

    public PageDTO<ScrapeData> deletedData(int page) {
        Pageable pageable = PageRequest.of(page - 1, GeneralConstants.PAGE_SIZE, Sort.by("createdAt").descending());
        Page<ScrapeData> pageContent = scrapeDataRepository.findByDeletedTrue(pageable);
        return new PageDTO<>(
                pageContent.getContent(),
                pageContent.getTotalPages(),
                page);
    }

    public PageDTO<ScrapeData> allDataIncludingDeleted(int page) {
        Pageable pageable = PageRequest.of(page - 1, GeneralConstants.PAGE_SIZE, Sort.by("createdAt").descending());
        Page<ScrapeData> pageContent = scrapeDataRepository.findAll(pageable);
        return new PageDTO<>(
                pageContent.getContent(),
                pageContent.getTotalPages(),
                page);
    }

    public ScrapeData getData(UUID dataId) {
        return scrapeDataRepository.findById(dataId).orElseThrow(
                () -> new ResourceNotFoundException("ScrapeData with id [%s] was not found!".formatted(dataId)));
    }

    public ScrapeData getDataIncludingDeleted(UUID dataId) {
        return scrapeDataRepository.findById(dataId).orElseThrow(
                () -> new ResourceNotFoundException("ScrapeData with id [%s] was not found!".formatted(dataId)));
    }

    public void createScrapeData(String pageData, UUID scrapeJobId) {
        ScrapeJob scrapeJob = scrapeJobRepository.findById(scrapeJobId).orElseThrow(
                () -> new ResourceNotFoundException("ScrapeJob with id [%s] was not found!".formatted(scrapeJobId)));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm");
        String fileName = dateFormat.format(LocalDateTime.now()) + ".txt";
        fileManagement.CreateAFile(scrapeJobId, scrapeJob.getAttempt(), fileName, pageData);
        ScrapeData scrapeData = new ScrapeData(fileName, scrapeJob.getAttempt(), scrapeJob);
        scrapeDataRepository.save(scrapeData);
    }

    public void updateScrapeData(ScrapeData scrapeData) {
        scrapeDataRepository.save(scrapeData);
    }

    public void softDeleteData(UUID scrapeDataId) {
        scrapeDataRepository.softDelete(scrapeDataId);
    }

    public void restoreData(UUID scrapeDataId) {
        scrapeDataRepository.restore(scrapeDataId);
    }

    public void deleteData(UUID scrapeDataId) {
        scrapeDataRepository.deleteById(scrapeDataId);
    }

}