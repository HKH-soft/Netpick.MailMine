package ir.netpick.platform.financefarm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FinanceFarm - Accounting and Invoicing DTO
 */
public record InvoiceDTO(
    UUID id,
    String invoiceNumber,
    String customerName,
    Double totalAmount,
    Double taxAmount,
    String currency,
    String status,
    LocalDateTime issueDate,
    LocalDateTime dueDate,
    LocalDateTime paidAt,
    UUID createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}