package com.ajthapa.user;

public record CreateAppUserRequest(
        String name,
        String email
) {
}
