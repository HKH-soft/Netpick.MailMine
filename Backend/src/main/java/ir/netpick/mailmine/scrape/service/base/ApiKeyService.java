package ir.netpick.mailmine.scrape.service.base;

import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.common.utils.PageDTOMapper;
import ir.netpick.mailmine.scrape.dto.ApiKeyRequest;
import ir.netpick.mailmine.scrape.dto.ApiKeyResponse;
import ir.netpick.mailmine.scrape.mapper.ApiKeyDTOMapper;
import ir.netpick.mailmine.scrape.model.ApiKey;
import ir.netpick.mailmine.scrape.repository.ApiKeyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyDTOMapper apiKeyDTOMapper;

    @Cacheable(value = "apiKeys", key = "'page-' + #pageNumber")
    public PageDTO<ApiKeyResponse> allKeys(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<ApiKey> page = apiKeyRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page, apiKeyDTOMapper);
    }

    public PageDTO<ApiKeyResponse> deletedKeys(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<ApiKey> page = apiKeyRepository.findByDeletedTrue(pageable);
        return PageDTOMapper.map(page, apiKeyDTOMapper);
    }

    public PageDTO<ApiKeyResponse> allKeysIncludingDeleted(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<ApiKey> page = apiKeyRepository.findAll(pageable);
        return PageDTOMapper.map(page, apiKeyDTOMapper);
    }

    @Cacheable(value = "apiKey", key = "#id")
    public ApiKey getKey(@NotNull UUID id) {
        return apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey with ID [%s] not found.".formatted(id)));
    }

    public ApiKey getKeyIncludingDeleted(@NotNull UUID id) {
        return apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey with ID [%s] not found.".formatted(id)));
    }

    @CacheEvict(value = { "apiKey", "apiKeys" }, allEntries = true)
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

    @CacheEvict(value = { "apiKey", "apiKeys" }, allEntries = true)
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

    @CacheEvict(value = { "apiKey", "apiKeys" }, allEntries = true)
    public void softDeleteKey(@NotNull UUID id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new ResourceNotFoundException("ApiKey with ID [%s] not found.".formatted(id));
        }
        apiKeyRepository.softDelete(id);
        log.info("Soft deleted ApiKey with ID: {}", id);
    }

    @CacheEvict(value = { "apiKey", "apiKeys" }, allEntries = true)
    public void restoreKey(@NotNull UUID id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new ResourceNotFoundException("ApiKey with ID [%s] not found.".formatted(id));
        }
        apiKeyRepository.restore(id);
        log.info("Restored ApiKey with ID: {}", id);
    }

    @CacheEvict(value = { "apiKey", "apiKeys" }, allEntries = true)
    public void deleteKey(@NotNull UUID id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new ResourceNotFoundException("ApiKey with ID [%s] not found.".formatted(id));
        }
        apiKeyRepository.deleteById(id);
        log.info("Deleted ApiKey with ID: {}", id);
    }
}