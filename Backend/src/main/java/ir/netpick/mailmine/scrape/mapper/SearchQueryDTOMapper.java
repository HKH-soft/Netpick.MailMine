package ir.netpick.mailmine.scrape.mapper;

import ir.netpick.mailmine.scrape.dto.ApiKeyResponse;
import ir.netpick.mailmine.scrape.dto.SearchQueryResponse;
import ir.netpick.mailmine.scrape.model.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class SearchQueryDTOMapper implements Function<SearchQuery, SearchQueryResponse> {

    @Override
    public SearchQueryResponse apply(SearchQuery searchQuery) {
        return new SearchQueryResponse(
                searchQuery.getSentence(),
                searchQuery.getLink_count(),
                searchQuery.getDescription(),
                searchQuery.getCreatedAt(),
                searchQuery.getUpdatedAt());
    }
}
