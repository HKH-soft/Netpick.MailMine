package ir.netpick.platform.inventoryfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.inventoryfarm.model.Product;
import ir.netpick.platform.inventoryfarm.model.StockMovement;
import ir.netpick.platform.inventoryfarm.model.StockMovementType;
import ir.netpick.platform.inventoryfarm.repository.ProductRepository;
import ir.netpick.platform.inventoryfarm.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public PageDTO<Product> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Product> page = productRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Product> getByWarehouse(UUID warehouseId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Product> page = productRepository.findByWarehouseIdAndDeletedFalse(warehouseId, pageable);
        return PageDTOMapper.map(page);
    }

    public Product getById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id [%s] was not found".formatted(productId)));
    }

    public Product getBySku(String sku) {
        return productRepository.findBySkuAndDeletedFalse(sku);
    }

    public Product create(Product product) {
        product.setId(null);
        if (product.getLastStockUpdate() == null) {
            product.setLastStockUpdate(LocalDateTime.now());
        }
        return productRepository.save(product);
    }

    public Product update(UUID productId, Product product) {
        Product existing = getById(productId);
        product.setId(productId);
        product.setCreatedAt(existing.getCreatedAt());
        return productRepository.save(product);
    }

    public void delete(UUID productId) {
        productRepository.softDelete(productId);
    }

    public void restore(UUID productId) {
        productRepository.restore(productId);
    }

    public Product adjustStock(UUID productId, int quantityChange, StockMovementType type, String reason, UUID movedBy) {
        Product product = getById(productId);
        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        product.setQuantity(newQuantity);
        product.setLastStockUpdate(LocalDateTime.now());
        productRepository.save(product);

        // Record movement
        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setQuantity(quantityChange);
        movement.setType(type);
        movement.setReason(reason);
        movement.setMovedBy(movedBy);
        stockMovementRepository.save(movement);

        return product;
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
}