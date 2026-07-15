package ir.netpick.platform.inventoryfarm.controller;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.inventoryfarm.dto.InventoryDTO;
import ir.netpick.platform.inventoryfarm.model.Product;
import ir.netpick.platform.inventoryfarm.model.StockMovementType;
import ir.netpick.platform.inventoryfarm.service.ProductService;
import ir.netpick.platform.gatekeeper.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * InventoryFarm - Stock and Warehouse Controller
 */
@RestController
@RequestMapping("/api/v1/inventoryfarm/products")
@RequiredArgsConstructor
public class InventoryController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageDTO<Product>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.getAll(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> getProduct(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.getBySku(sku));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Product>> getLowStockProducts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.create(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @RequestBody Product product,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> adjustStock(
            @PathVariable UUID id,
            @RequestParam int quantity,
            @RequestParam StockMovementType type,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.adjustStock(id, quantity, type, reason, user.getId()));
    }
}