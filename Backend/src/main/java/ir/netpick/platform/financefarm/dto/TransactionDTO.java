package ir.netpick.platform.financefarm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FinanceFarm - Transaction DTO
 */
public record TransactionDTO(
    UUID id,
    BigDecimal amount,
    String type,
    String category,
    String description,
    LocalDateTime date,
    UUID invoiceId,
    UUID createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}