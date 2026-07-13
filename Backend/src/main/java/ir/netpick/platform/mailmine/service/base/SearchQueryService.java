package ir.netpick.platform.mailmine.service.base;

import java.util.Objects;
import java.util.UUID;
import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.mailmine.dto.SearchQueryResponse;
import ir.netpick.platform.mailmine.mapper.SearchQueryDTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.mailmine.dto.SearchQueryRequest;
import ir.netpick.platform.mailmine.model.SearchQuery;
import ir.netpick.platform.mailmine.repository.SearchQueryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class SearchQueryService {

    private final SearchQueryRepository searchQueryRepository;
    private final SearchQueryDTOMapper searchQueryDTOMapper;

    public boolean isEmpty() {
        return searchQueryRepository.count() == 0;
    }

    @Cacheable(value = "searchQueries", key = "'page-' + #pageNumber")
    public PageDTO<SearchQueryResponse> allSearchQueries(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<SearchQuery> page = searchQueryRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page, searchQueryDTOMapper);
    }

    public PageDTO<SearchQueryResponse> deletedSearchQueries(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<SearchQuery> page = searchQueryRepository.findByDeletedTrue(pageable);
        return PageDTOMapper.map(page, searchQueryDTOMapper);

    }

    public PageDTO<SearchQueryResponse> allSearchQueriesIncludingDeleted(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<SearchQuery> page = searchQueryRepository.findAll(pageable);
        return PageDTOMapper.map(page, searchQueryDTOMapper);
    }

    @Cacheable(value = "searchQuery", key = "#id")
    public SearchQuery getSearchQuery(@NotNull UUID id) {
        return searchQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SearchQuery with ID [%s] not found.".formatted(id)));
    }

    public SearchQuery getSearchQueryIncludingDeleted(@NotNull UUID id) {
        return searchQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SearchQuery with ID [%s] not found.".formatted(id)));
    }

    @CacheEvict(value = { "searchQuery", "searchQueries" }, allEntries = true)
    public SearchQuery createSearchQuery(@Valid @NotNull SearchQueryRequest request) {
        SearchQuery searchQuery = new SearchQuery(
                request.sentence(),
                request.description());
        SearchQuery saved = searchQueryRepository.save(searchQuery);
        log.info("Created SearchQuery with ID: {}", saved.getId());
        return saved;
    }

    @CacheEvict(value = { "searchQuery", "searchQueries" }, allEntries = true)
    public SearchQuery updateSearchQuery(@NotNull UUID id, @Valid @NotNull SearchQueryRequest request) {
        SearchQuery existing = getSearchQuery(id);

        boolean changed = false;

        if (Objects.nonNull(request.sentence()) && !Objects.equals(request.sentence(), existing.getSentence())) {
            existing.setSentence(request.sentence());
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

    @CacheEvict(value = { "searchQuery", "searchQueries" }, allEntries = true)
    public void softDeleteSearchQuery(@NotNull UUID id) {
        if (!searchQueryRepository.existsById(id)) {
            throw new ResourceNotFoundException("SearchQuery with ID [%s] not found.".formatted(id));
        }
        searchQueryRepository.softDelete(id);
        log.info("Soft deleted SearchQuery with ID: {}", id);
    }

    @CacheEvict(value = { "searchQuery", "searchQueries" }, allEntries = true)
    public void restoreSearchQuery(@NotNull UUID id) {
        if (!searchQueryRepository.existsById(id)) {
            throw new ResourceNotFoundException("SearchQuery with ID [%s] not found.".formatted(id));
        }
        searchQueryRepository.restore(id);
        log.info("Restored SearchQuery with ID: {}", id);
    }

    @CacheEvict(value = { "searchQuery", "searchQueries" }, allEntries = true)
    public void deleteSearchQuery(@NotNull UUID id) {
        if (!searchQueryRepository.existsById(id)) {
            throw new ResourceNotFoundException("SearchQuery with ID [%s] not found.".formatted(id));
        }
        searchQueryRepository.deleteById(id);
        log.info("Deleted SearchQuery with ID: {}", id);
    }
}








