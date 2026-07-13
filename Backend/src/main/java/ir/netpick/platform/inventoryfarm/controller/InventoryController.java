package ir.netpick.platform.inventoryfarm.controller;

import ir.netpick.platform.inventoryfarm.dto.InventoryDTO;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * InventoryFarm - Stock and Warehouse Controller
 */
@RestController
@RequestMapping("/api/v1/inventoryfarm/inventory")
@RequiredArgsConstructor
public class InventoryController {

    // TODO: Add InventoryService dependency

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventory(@AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getInventoryItem(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(null);
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventoryItem(@RequestBody InventoryDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDTO> updateInventoryItem(@PathVariable UUID id, @RequestBody InventoryDTO request, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}