package ir.netpick.platform.inventoryfarm.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_quantity")
    private Integer minQuantity = 0;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "last_stock_update")
    private LocalDateTime lastStockUpdate;

    public Product() {
    }
}