package com.ajthapa.auth;

import com.ajthapa.user.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void generatedTokenCanBeDecodedAndContainsExpectedClaims() {
        AppUser user = new AppUser("Test User", "test@example.com", "encoded-password");
        user.setId(42L);

        Jwt jwt = jwtDecoder.decode(jwtService.generateToken(user));

        assertEquals("42", jwt.getSubject());
        assertEquals("test@example.com", jwt.getClaimAsString("email"));
        assertEquals("https://fintrack.test", jwt.getIssuer().toString());
        assertNotNull(jwt.getExpiresAt());
    }
}
