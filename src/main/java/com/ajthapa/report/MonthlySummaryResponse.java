package com.ajthapa.report;
import java.math.BigDecimal;

public record MonthlySummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netSavings
) {
}
