package com.ajthapa.transaction;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        Long accountId,
        String description,
        BigDecimal amount,
        TransactionType type
) {

}
