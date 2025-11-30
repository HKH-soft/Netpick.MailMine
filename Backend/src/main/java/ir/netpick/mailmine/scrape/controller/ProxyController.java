package ir.netpick.mailmine.scrape.controller;

import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.enums.ProxyProtocol;
import ir.netpick.mailmine.scrape.dto.ProxyRequest;
import ir.netpick.mailmine.scrape.dto.ProxyResponse;
import ir.netpick.mailmine.scrape.mapper.ProxyDTOMapper;
import ir.netpick.mailmine.scrape.model.Proxy;
import ir.netpick.mailmine.scrape.service.base.ProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/proxies")
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;
    private final ProxyDTOMapper proxyDTOMapper;

    // ==================== CRUD ====================

    @GetMapping
    public ResponseEntity<PageDTO<ProxyResponse>> getAllProxies(
            @RequestParam(defaultValue = "1") int page) {
        PageDTO<Proxy> proxies = proxyService.allProxies(page);
        return ResponseEntity.ok(new PageDTO<>(
                proxies.content().stream().map(proxyDTOMapper).collect(Collectors.toList()),
                proxies.totalPages(),
                proxies.currentPage()));
    }

    @GetMapping("/deleted")
    public ResponseEntity<PageDTO<ProxyResponse>> getDeletedProxies(
            @RequestParam(defaultValue = "1") int page) {
        PageDTO<Proxy> proxies = proxyService.deletedProxies(page);
        return ResponseEntity.ok(new PageDTO<>(
                proxies.content().stream().map(proxyDTOMapper).collect(Collectors.toList()),
                proxies.totalPages(),
                proxies.currentPage()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProxyResponse> getProxy(@PathVariable UUID id) {
        Proxy proxy = proxyService.getProxy(id);
        return ResponseEntity.ok(proxyDTOMapper.apply(proxy));
    }

    @PostMapping
    public ResponseEntity<ProxyResponse> createProxy(@RequestBody ProxyRequest request) {
        Proxy proxy = new Proxy(
                request.protocol() != null ? request.protocol() : ProxyProtocol.SOCKS5,
                request.host(),
                request.port(),
                request.username(),
                request.password());
        proxy.setDescription(request.description());
        Proxy saved = proxyService.createProxy(proxy);
        return ResponseEntity.status(HttpStatus.CREATED).body(proxyDTOMapper.apply(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProxyResponse> updateProxy(
            @PathVariable UUID id,
            @RequestBody ProxyRequest request) {
        Proxy updates = new Proxy();
        updates.setProtocol(request.protocol());
        updates.setHost(request.host());
        updates.setPort(request.port());
        updates.setUsername(request.username());
        updates.setPassword(request.password());
        updates.setDescription(request.description());

        Proxy updated = proxyService.updateProxy(id, updates);
        return ResponseEntity.ok(proxyDTOMapper.apply(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteProxy(@PathVariable UUID id) {
        proxyService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreProxy(@PathVariable UUID id) {
        proxyService.restore(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteProxy(@PathVariable UUID id) {
        proxyService.deleteProxy(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Import ====================

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importProxies(@RequestParam("file") MultipartFile file) {
        int imported = proxyService.importFromMultipartFile(file);
        return ResponseEntity.ok(Map.of(
                "message", "Import completed",
                "imported", imported));
    }

    @PostMapping("/import/text")
    public ResponseEntity<Map<String, Object>> importProxiesFromText(@RequestBody String proxyList) {
        String[] lines = proxyList.split("\n");
        int imported = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                try {
                    Proxy proxy = Proxy.fromUrl(trimmed);
                    proxyService.createProxy(proxy);
                    imported++;
                } catch (Exception e) {
                    // Skip invalid lines
                }
            }
        }
        return ResponseEntity.ok(Map.of(
                "message", "Import completed",
                "imported", imported));
    }

    // ==================== Testing ====================

    @PostMapping("/{id}/test")
    public ResponseEntity<ProxyResponse> testProxy(@PathVariable UUID id) {
        Proxy tested = proxyService.testProxy(id);
        return ResponseEntity.ok(proxyDTOMapper.apply(tested));
    }

    @PostMapping("/test/untested")
    public ResponseEntity<Map<String, String>> testUntestedProxies() {
        proxyService.testUntestedProxies();
        return ResponseEntity.accepted().body(Map.of(
                "message", "Testing untested proxies in background"));
    }

    @PostMapping("/test/active")
    public ResponseEntity<Map<String, String>> testActiveProxies() {
        proxyService.testActiveProxies();
        return ResponseEntity.accepted().body(Map.of(
                "message", "Re-testing active proxies in background"));
    }

    // ==================== Stats ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(proxyService.getProxyStats());
    }
}
