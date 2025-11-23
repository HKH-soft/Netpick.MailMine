package ir.netpick.mailmine.scrape.service.base;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.scrape.dto.SearchQueryResponse;
import ir.netpick.mailmine.scrape.mapper.SearchQueryDTOMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.dto.SearchQueryRequest;
import ir.netpick.mailmine.scrape.model.SearchQuery;
import ir.netpick.mailmine.scrape.repository.SearchQueryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Validated
@Service
@RequiredArgsConstructor
public class SearchQueryService {

    private final SearchQueryRepository searchQueryRepository;
    private final SearchQueryDTOMapper searchQueryDTOMapper;

    @Value("${env.page-size:10}")
    private int pageSize;

    public boolean isEmpty() {
        return searchQueryRepository.count() == 0;
    }

    public PageDTO<SearchQueryResponse> allSearchQueries(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1 , pageSize, Sort.by("createdAt").descending());
        Page<SearchQuery> page =  searchQueryRepository.findAll(pageable);
        return new PageDTO<>(
                page.getContent()
                        .stream()
                        .map(searchQueryDTOMapper)
                        .collect(Collectors.toList()),
                page.getTotalPages(),
                page.getNumber() + 1);
    }

    public SearchQuery getSearchQuery(@NotNull UUID id) {
        return searchQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SearchQuery with ID [%s] not found.".formatted(id)));
    }

    public SearchQuery createSearchQuery(@Valid @NotNull SearchQueryRequest request) {
        SearchQuery searchQuery = new SearchQuery(
                request.sentence(),
                request.link_count(),
                request.description());
        SearchQuery saved = searchQueryRepository.save(searchQuery);
        log.info("Created SearchQuery with ID: {}", saved.getId());
        return saved;
    }

    public SearchQuery updateSearchQuery(@NotNull UUID id, @Valid @NotNull SearchQueryRequest request) {
        SearchQuery existing = getSearchQuery(id);

        boolean changed = false;

        if (Objects.nonNull(request.sentence()) && !Objects.equals(request.sentence(), existing.getSentence())) {
            existing.setSentence(request.sentence());
            changed = true;
        }

        if (Objects.nonNull(request.link_count()) && !Objects.equals(request.link_count(), existing.getLink_count())) {
            existing.setLink_count(request.link_count());
            changed = true;
        }

        if (Objects.nonNull(request.description())
                && !Objects.equals(request.description(), existing.getDescription())) {
            existing.setDescription(request.description());
            changed = true;
        }

        if (!changed) {
            log.warn("No changes found in SearchQuery update request for ID: {}", id);
            return existing; // Or throw if strict
        }

        SearchQuery saved = searchQueryRepository.save(existing);
        log.info("Updated SearchQuery with ID: {}", id);
        return saved;
    }

    public void deleteSearchQuery(@NotNull UUID id) {
        if (!searchQueryRepository.existsById(id)) {
            throw new ResourceNotFoundException("SearchQuery with ID [%s] not found.".formatted(id));
        }
        searchQueryRepository.deleteById(id);
        log.info("Deleted SearchQuery with ID: {}", id);
    }
}