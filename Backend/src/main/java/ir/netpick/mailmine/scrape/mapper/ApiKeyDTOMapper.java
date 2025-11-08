package ir.netpick.mailmine.scrape.mapper;

import ir.netpick.mailmine.scrape.dto.ApiKeyResponse;
import ir.netpick.mailmine.scrape.model.ApiKey;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ApiKeyDTOMapper implements Function<ApiKey , ApiKeyResponse> {

    @Override
    public ApiKeyResponse apply(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getKey(),
                apiKey.getPoint(),
                apiKey.getApiLink(),
                apiKey.getDescription(),
                apiKey.getCreatedAt(),
                apiKey.getUpdatedAt());
    }
}
