package com.ajthapa.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public record CreateAppUserRequest(
        @NotBlank @Size(max=255) String name,
        @NotBlank @Size(max=254) @Email String email
) {
}
