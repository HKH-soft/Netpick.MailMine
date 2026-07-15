package ir.netpick.platform.inventoryfarm.controller;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.inventoryfarm.dto.StockMovementDTO;
import ir.netpick.platform.inventoryfarm.model.StockMovement;
import ir.netpick.platform.inventoryfarm.service.StockMovementService;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * InventoryFarm - Stock Movement Controller
 */
@RestController
@RequestMapping("/api/v1/inventoryfarm/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageDTO<StockMovement>> getAllMovements(
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stockMovementService.getAll(page));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageDTO<StockMovement>> getMovementsByProduct(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stockMovementService.getByProduct(productId, page));
    }

    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockMovement>> getMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stockMovementService.getByDateRange(startDate, endDate));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StockMovement> getMovement(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stockMovementService.getById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StockMovement> createMovement(@RequestBody StockMovement movement, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stockMovementService.create(movement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMovement(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        stockMovementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}