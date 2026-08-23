package com.ajthapa.user;

import java.time.LocalDateTime;

public record AppUserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {
}
