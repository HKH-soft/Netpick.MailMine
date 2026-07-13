package ir.netpick.platform.inventoryfarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * InventoryFarm - Stock and Warehouse DTO
 */
public record InventoryDTO(
    UUID id,
    String productName,
    String sku,
    Integer quantity,
    Integer minQuantity,
    Double unitPrice,
    String currency,
    UUID warehouseId,
    UUID categoryId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastStockUpdate
) {}