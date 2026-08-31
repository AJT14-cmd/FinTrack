package com.ajthapa.budget;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String month,
        BigDecimal limitAmount
) {
}
