package com.ajthapa.account;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateAccountRequest(
        @NotBlank String name,
        @NotNull AccountType type,
        @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal balance
) {
}
