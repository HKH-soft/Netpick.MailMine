package ir.netpick.mailmine.scrape.service;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import ir.netpick.mailmine.common.BasePageRecord;
import ir.netpick.mailmine.scrape.dto.ApiKeyResponse;
import ir.netpick.mailmine.scrape.mapper.ApiKeyDTOMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.dto.ApiKeyRequest;
import ir.netpick.mailmine.scrape.model.ApiKey;
import ir.netpick.mailmine.scrape.repository.ApiKeyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyDTOMapper apiKeyDTOMapper;

    @Value("${env.page-size:10}")
    private int pageSize;

    public BasePageRecord<ApiKeyResponse> allKeys(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("createdAt").descending());
        Page<ApiKey> page =  apiKeyRepository.findAll(pageable);
        return new BasePageRecord<>(
                page.getContent()
                        .stream()
                        .map(apiKeyDTOMapper)
                        .collect(Collectors.toList()),
                page.getTotalPages(),
                page.getNumber() + 1
        );
    }

    public ApiKey getKey(@NotNull UUID id) {
        return apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey with ID [%s] not found.".formatted(id)));
    }

    public ApiKey createKey(@Valid @NotNull ApiKeyRequest apiKeyRequest) {
        ApiKey apiKey = new ApiKey(
                apiKeyRequest.key(),
                apiKeyRequest.point(),
                apiKeyRequest.apiLink(),
                apiKeyRequest.searchEngineId(),
                apiKeyRequest.description());
        ApiKey saved = apiKeyRepository.save(apiKey);
        log.info("Created ApiKey with ID: {}", saved.getId());
        return saved;
    }

    public ApiKey updateKey(@NotNull UUID id, @Valid @NotNull ApiKeyRequest updatedApiKey) {
        ApiKey existing = getKey(id);

        if (Objects.nonNull(updatedApiKey.key())) {
            existing.setKey(updatedApiKey.key());
        }
        if (Objects.nonNull(updatedApiKey.point())) {
            existing.setPoint(updatedApiKey.point());
        }
        if (Objects.nonNull(updatedApiKey.apiLink())) {
            existing.setApiLink(updatedApiKey.apiLink());
        }
        if (Objects.nonNull(updatedApiKey.searchEngineId())) {
            existing.setSearchEngineId(updatedApiKey.searchEngineId());
        }
        if (Objects.nonNull(updatedApiKey.description())) {
            existing.setDescription(updatedApiKey.description());
        }

        ApiKey saved = apiKeyRepository.save(existing);
        log.info("Updated ApiKey with ID: {}", id);
        return saved;
    }

    public void deleteKey(@NotNull UUID id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new ResourceNotFoundException("ApiKey with ID [%s] not found.".formatted(id));
        }
        apiKeyRepository.deleteById(id);
        log.info("Deleted ApiKey with ID: {}", id);
    }
}