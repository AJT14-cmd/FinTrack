package com.ajthapa.budget;

import java.math.BigDecimal;

public record BudgetStatusResponse(
        Long categoryId,
        String categoryName,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BudgetStatus status
) {
}
