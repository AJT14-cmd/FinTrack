package com.ajthapa.budget;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateBudgetRequest(
        @NotNull Long categoryId,
        @NotNull Long appUserId,
        @NotBlank String month,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal limitAmount
) {
}
