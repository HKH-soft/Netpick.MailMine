package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.scrape.dto.ApiKeyRequest;
import ir.netpick.mailmine.scrape.service.base.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scrape/api_keys")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ApiKeyController {

    private final ApiKeyService ApiKeyService;

    @GetMapping("")
    public ResponseEntity<?> getApiKeys(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(ApiKeyService.allKeys(page));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getApiKey(@PathVariable UUID id) {
        return ResponseEntity.ok().body(ApiKeyService.getKey(id));
    }

    @PostMapping("")
    public ResponseEntity<?> createApiKey(@RequestBody ApiKeyRequest request) {
        return ResponseEntity.ok().body(ApiKeyService.createKey(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateApiKey(@PathVariable UUID id, @RequestBody ApiKeyRequest request) {
        return ResponseEntity.ok().body(ApiKeyService.updateKey(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteApiKey(@PathVariable UUID id) {
        ApiKeyService.deleteKey(id);
        return ResponseEntity.noContent().build();
    }
}
