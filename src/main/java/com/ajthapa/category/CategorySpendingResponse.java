package com.ajthapa.category;

import java.math.BigDecimal;

public record CategorySpendingResponse(
        Long id,
        String name,
        BigDecimal amount
) {
}
