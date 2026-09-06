package com.ajthapa.account;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        Long accountId,
        String accountName,
        AccountType accountType,
        BigDecimal balance
) {
}
