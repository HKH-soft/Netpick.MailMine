package ir.netpick.platform.mailmine.mapper;

import ir.netpick.platform.mailmine.dto.SearchQueryResponse;
import ir.netpick.platform.mailmine.model.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class SearchQueryDTOMapper implements Function<SearchQuery, SearchQueryResponse> {

    @Override
    public SearchQueryResponse apply(SearchQuery searchQuery) {
        return new SearchQueryResponse(
                searchQuery.getSentence(),
                searchQuery.getLinkCount(),
                searchQuery.getDescription(),
                searchQuery.getCreatedAt(),
                searchQuery.getUpdatedAt());
    }
}









