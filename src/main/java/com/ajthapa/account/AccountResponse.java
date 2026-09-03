package com.ajthapa.account;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String name,
        AccountType type,
        BigDecimal balance,
        Long appUserId,
        String appUserName
) {
}
