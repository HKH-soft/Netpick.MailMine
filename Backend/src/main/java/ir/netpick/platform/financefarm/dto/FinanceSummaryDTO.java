package ir.netpick.platform.financefarm.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * FinanceFarm - Finance Summary DTO
 */
public record FinanceSummaryDTO(
    BigDecimal totalRevenue,
    BigDecimal totalExpenses,
    BigDecimal profit,
    Map<String, BigDecimal> revenueByMonth,
    Map<String, BigDecimal> expensesByMonth,
    Long totalInvoices,
    Long paidInvoices,
    Long overdueInvoices
) {}