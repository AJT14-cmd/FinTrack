package com.ajthapa.auth;

public record LoginResponse(
    String token,
    String tokenType,
    long expiresIn
) {
}
