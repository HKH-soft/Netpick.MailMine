package ir.netpick.mailmine.scrape.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ir.netpick.mailmine.scrape.dto.ApiKeyRequest;
import ir.netpick.mailmine.scrape.service.base.ApiKeyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scrape/api_keys")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ApiKeyController {

    private final ApiKeyService ApiKeyService;

    @GetMapping("")
    public ResponseEntity<?> getApiKeys(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(ApiKeyService.allKeys(page));
    }
    
    @GetMapping("/deleted")
    public ResponseEntity<?> getDeletedApiKeys(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(ApiKeyService.deletedKeys(page));
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllApiKeysIncludingDeleted(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(ApiKeyService.allKeysIncludingDeleted(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getApiKey(@PathVariable UUID id) {
        return ResponseEntity.ok().body(ApiKeyService.getKey(id));
    }
    
    @GetMapping("/deleted/{id}")
    public ResponseEntity<?> getDeletedApiKey(@PathVariable UUID id) {
        return ResponseEntity.ok().body(ApiKeyService.getKeyIncludingDeleted(id));
    }

    @PostMapping("")
    public ResponseEntity<?> createApiKey(@RequestBody ApiKeyRequest request) {
        return ResponseEntity.ok().body(ApiKeyService.createKey(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateApiKey(@PathVariable UUID id, @RequestBody ApiKeyRequest request) {
        return ResponseEntity.ok().body(ApiKeyService.updateKey(id, request));
    }
    
    @PutMapping("{id}/restore")
    public ResponseEntity<?> restoreApiKey(@PathVariable UUID id) {
        ApiKeyService.restoreKey(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteApiKey(@PathVariable UUID id) {
        ApiKeyService.softDeleteKey(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("{id}/full_delete")
    public ResponseEntity<?> fullDeleteApiKey(@PathVariable UUID id) {
        ApiKeyService.deleteKey(id);
        return ResponseEntity.noContent().build();
    }
}