package ir.netpick.mailmine.scrape.service.base;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ir.netpick.mailmine.common.exception.RequestValidationException;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.model.ScrapeJob;
import ir.netpick.mailmine.scrape.repository.ScrapeJobRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class ScrapeJobService {

    private final ScrapeJobRepository scrapeJobRepository;

    public boolean isEmpty() {
        return scrapeJobRepository.count() == 0;
    }

    public boolean scrapeJobExists(@NotNull UUID id) {
        return scrapeJobRepository.existsById(id);
    }

    public boolean scrapeJobExists(@NotNull String link) {
        return scrapeJobRepository.existsByLink(link);
    }

    public PageDTO<ScrapeJob> allJobs(@NotNull int page) {
        Pageable pageable = PageRequest.of(page - 1, GeneralConstants.PAGE_SIZE, Sort.by("createdAt").descending());
        Page<ScrapeJob> pageContent = scrapeJobRepository.findByDeletedFalse(pageable);
        return new PageDTO<>(
                pageContent.getContent(),
                pageContent.getTotalPages(),
                page);
    }

    public PageDTO<ScrapeJob> deletedJobs(@NotNull int page) {
        Pageable pageable = PageRequest.of(page - 1, GeneralConstants.PAGE_SIZE, Sort.by("createdAt").descending());
        Page<ScrapeJob> pageContent = scrapeJobRepository.findByDeletedTrue(pageable);
        return new PageDTO<>(
                pageContent.getContent(),
                pageContent.getTotalPages(),
                page);
    }

    public PageDTO<ScrapeJob> allJobsIncludingDeleted(@NotNull int page) {
        Pageable pageable = PageRequest.of(page - 1, GeneralConstants.PAGE_SIZE, Sort.by("createdAt").descending());
        Page<ScrapeJob> pageContent = scrapeJobRepository.findAll(pageable);
        return new PageDTO<>(
                pageContent.getContent(),
                pageContent.getTotalPages(),
                page);
    }

    public ScrapeJob getJob(@NotNull UUID id) {
        return scrapeJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScrapeJob with ID [%s] not found.".formatted(id)));
    }

    public ScrapeJob getJobIncludingDeleted(@NotNull UUID id) {
        return scrapeJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScrapeJob with ID [%s] not found.".formatted(id)));
    }

    public ScrapeJob getJob(@NotNull String link) {
        return scrapeJobRepository.findByLink(link)
                .orElseThrow(
                        () -> new ResourceNotFoundException("ScrapeJob with link [%s] not found.".formatted(link)));
    }

    public void createJob(@NotNull @Valid ScrapeJob scrapeJob) {
        scrapeJobRepository.save(scrapeJob);
    }

    public void createJob(@NotNull String link, String description) {
        ScrapeJob scrapeJob = new ScrapeJob(link, description);
        scrapeJobRepository.save(scrapeJob);
        log.info("Created ScrapeJob for link: {}", link);
    }

    public void createJobsByList(@NotNull @Valid List<String> urls, @NotNull @Valid List<String> titles) {
        if (urls.size() != titles.size()) {
            throw new RequestValidationException("URLs and titles lists must be of equal size.");
        }

        Set<String> existingLinks = scrapeJobRepository.findAllByLinkIn(urls)
                .stream()
                .map(ScrapeJob::getLink)
                .collect(Collectors.toSet());

        List<ScrapeJob> newJobs = IntStream.range(0, urls.size())
                .filter(i -> !existingLinks.contains(urls.get(i)))
                .mapToObj(i -> new ScrapeJob(urls.get(i), titles.get(i)))
                .toList();

        if (!newJobs.isEmpty()) {
            scrapeJobRepository.saveAll(newJobs);
            log.info("Created {} new ScrapeJobs.", newJobs.size());
        } else {
            log.info("No new ScrapeJobs to create; all links exist.");
        }
    }

    public void updateScrapeJob(@NotNull UUID jobId, @NotNull ScrapeJob updates) {
        ScrapeJob existing = scrapeJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("ScrapeJob with ID [%s] not found.".formatted(jobId)));

        boolean changed = false;

        if (updates.getLink() != null && !Objects.equals(updates.getLink(), existing.getLink())) {
            existing.setLink(updates.getLink());
            changed = true;
        }

        if (updates.getAttempt() != null && !Objects.equals(updates.getAttempt(), existing.getAttempt())) {
            existing.setAttempt(updates.getAttempt());
            changed = true;
        }

        if (updates.getDescription() != null && !Objects.equals(updates.getDescription(), existing.getDescription())) {
            existing.setDescription(updates.getDescription());
            changed = true;
        }

        if (updates.getScrapeFailed() != null
                && !Objects.equals(updates.getScrapeFailed(), existing.getScrapeFailed())) {
            existing.setScrapeFailed(updates.getScrapeFailed());
            changed = true;
        }

        if (!changed) {
            log.debug("No changes detected for ScrapeJob with ID: {}", jobId);
            return; // No changes, just return without throwing
        }

        scrapeJobRepository.save(existing);
        log.info("Updated ScrapeJob with ID: {}", jobId);
    }

    public void softDelete(@NotNull UUID jobId) {
        scrapeJobRepository.softDelete(jobId);
    }

    public void restore(@NotNull UUID jobId) {
        scrapeJobRepository.restore(jobId);
    }

    public void deleteJob(@NotNull UUID jobId) {
        scrapeJobRepository.deleteById(jobId);
    }

}