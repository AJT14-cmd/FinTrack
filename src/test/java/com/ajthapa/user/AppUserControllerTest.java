package com.ajthapa.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserControllerTest {

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private AppUserController appUserController;

    @Test
    void currentUserUsesAuthenticatedJwtSubject() {
        AppUserResponse expected = new AppUserResponse(
                42L, "Test User", "test@example.com", LocalDateTime.now());
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        when(appUserService.getUsersById(42L)).thenReturn(expected);

        AppUserResponse actual = appUserController.getCurrentUser(jwt);

        assertSame(expected, actual);
        verify(appUserService).getUsersById(42L);
    }
}
