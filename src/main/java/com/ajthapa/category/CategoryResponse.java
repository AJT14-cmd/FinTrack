package com.ajthapa.category;

public record CategoryResponse(
        Long id,
        String name,
        CategoryType type
) {
}
