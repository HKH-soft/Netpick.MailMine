package ir.netpick.mailmine.scrape.service.base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import ir.netpick.mailmine.common.PageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Value;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.file.FileManagement;
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

    @Value("${env.page-size:10}")
    private int pageSize;

    public List<ScrapeData> findUnparsed(){
        return scrapeDataRepository.findByParsedFalse();
    }

    public List<ScrapeData> allDatas() {
        return scrapeDataRepository.findAll();
    }

    public PageDTO<ScrapeData> allDatas(int page) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
        Page<ScrapeData> pageContent = scrapeDataRepository.findAll(pageable);
        return new PageDTO<>(
                pageContent.getContent(),
                pageContent.getTotalPages(),
                page
                );
    }
    public ScrapeData getData(UUID dataId) {
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

    public void updateScrapeData(ScrapeData scrapeData){
        scrapeDataRepository.save(scrapeData);
    }

    public void deleteScrapeData(UUID scrapeJobId){
        scrapeDataRepository.deleteById(scrapeJobId);
    }

}
