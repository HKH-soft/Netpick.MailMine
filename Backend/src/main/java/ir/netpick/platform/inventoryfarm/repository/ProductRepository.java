package ir.netpick.platform.inventoryfarm.repository;

import ir.netpick.platform.inventoryfarm.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // Find all non-deleted products with pagination
    Page<Product> findByDeletedFalse(Pageable pageable);

    // Find products by warehouse with pagination
    Page<Product> findByWarehouseIdAndDeletedFalse(UUID warehouseId, Pageable pageable);

    // Find product by SKU
    Product findBySkuAndDeletedFalse(String sku);

    // Find low stock products
    @Query("select p from Product p where p.deleted = false and p.quantity <= p.minQuantity")
    List<Product> findLowStockProducts();

    // Soft delete
    @Transactional
    @Modifying
    @Query("update Product p set p.deleted = true where p.deleted = false and p.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update Product p set p.deleted = false where p.id = ?1 and p.deleted = true")
    void restore(UUID id);
}