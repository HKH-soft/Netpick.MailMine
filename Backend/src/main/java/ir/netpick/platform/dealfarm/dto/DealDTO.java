package ir.netpick.platform.dealfarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DealFarm - CRM and Sales Pipeline DTO
 */
public record DealDTO(
    UUID id,
    String title,
    String description,
    String stage,
    Double value,
    String currency,
    UUID contactId,
    UUID ownerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime closedAt,
    Integer probability,
    LocalDateTime expectedCloseDate
) {}