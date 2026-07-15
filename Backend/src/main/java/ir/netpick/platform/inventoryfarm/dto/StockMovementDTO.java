package ir.netpick.platform.inventoryfarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * InventoryFarm - Stock Movement DTO
 */
public record StockMovementDTO(
    UUID id,
    UUID productId,
    Integer quantity,
    String type,
    String reason,
    UUID movedBy,
    LocalDateTime movementDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}