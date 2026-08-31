package com.ajthapa.report;
import com.ajthapa.category.CategorySpendingResponse;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netSavings,
        List<CategorySpendingResponse> categorySpendingResponse
) {
}
