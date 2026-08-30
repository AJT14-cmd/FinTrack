package com.ajthapa.transaction;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull Long categoryId,
        @NotNull Long accountId,
        @NotBlank String description,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount,
        @NotNull TransactionType type
) {

}
