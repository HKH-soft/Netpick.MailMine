package ir.netpick.platform.financefarm.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * FinanceFarm - Invoice Line Item DTO
 */
public record InvoiceLineItemDTO(
    UUID id,
    UUID invoiceId,
    String description,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal total
) {}