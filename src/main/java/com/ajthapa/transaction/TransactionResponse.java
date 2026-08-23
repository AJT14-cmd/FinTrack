package com.ajthapa.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long accountId,
        String description,
        BigDecimal amount,
        TransactionType type,
        LocalDateTime transactionDateTime
) {

}
