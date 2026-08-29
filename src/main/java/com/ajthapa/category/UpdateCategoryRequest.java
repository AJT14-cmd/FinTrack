package com.ajthapa.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCategoryRequest(
        @NotBlank String name,
        @NotNull CategoryType type
) {
}
