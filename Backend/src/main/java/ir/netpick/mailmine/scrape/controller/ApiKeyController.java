package ir.netpick.mailmine.scrape.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.netpick.mailmine.scrape.dto.ApiKeyRequest;
import ir.netpick.mailmine.scrape.service.base.ApiKeyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ApiKeyController {

    private final ApiKeyService ApiKeyService;

    @GetMapping("/apikey")
    public ResponseEntity<?> getApiKeys(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(ApiKeyService.allKeys(page));
    }

    @GetMapping("/apikey/{id}")
    public ResponseEntity<?> getApiKey(@PathVariable UUID id) {
        return ResponseEntity.ok().body(ApiKeyService.getKey(id));
    }

    @PostMapping("/apikey")
    public ResponseEntity<?> createApiKey(@RequestBody ApiKeyRequest request) {
        return ResponseEntity.ok().body(ApiKeyService.createKey(request));
    }

    @PutMapping("/apikey/{id}")
    public ResponseEntity<?> updateApiKey(@PathVariable UUID id, @RequestBody ApiKeyRequest request) {
        return ResponseEntity.ok().body(ApiKeyService.updateKey(id, request));
    }

    @DeleteMapping("/apikey/{id}")
    public ResponseEntity<?> deleteApiKey(@PathVariable UUID id) {
        ApiKeyService.deleteKey(id);
        return ResponseEntity.noContent().build();
    }
}
