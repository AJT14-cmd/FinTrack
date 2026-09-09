package com.ajthapa.auth;

import com.ajthapa.user.AppUser;
import com.ajthapa.user.AppUserRepository;
import com.ajthapa.user.AppUserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrationHashesPasswordBeforeSavingUser() {
        RegisterRequest request = new RegisterRequest(
                " Test User ", " Test.User@Example.com ", "Password1!");
        when(appUserRepository.findByEmail("test.user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AppUserResponse response = authService.register(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertEquals("Test User", response.name());
        assertEquals("test.user@example.com", response.email());
        assertEquals("encoded-password", userCaptor.getValue().getPasswordHash());
        verify(passwordEncoder).encode("Password1!");
    }

    @Test
    void registrationRejectsDuplicateEmailBeforeHashingOrSaving() {
        RegisterRequest request = new RegisterRequest(
                "Test User", "test@example.com", "Password1!");
        AppUser existingUser = new AppUser("Existing User", "test@example.com", "encoded-password");
        when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        assertEquals("A user with email test@example.com already exists", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        LoginRequest request = new LoginRequest(" Test@Example.com ", "Password1!");
        AppUser user = new AppUser("Test User", "test@example.com", "encoded-password");
        user.setId(1L);
        when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("signed-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertEquals("signed-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
    }

    @Test
    void loginRejectsIncorrectPasswordWithoutCreatingToken() {
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword1!");
        AppUser user = new AppUser("Test User", "test@example.com", "encoded-password");
        when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword1!", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(any(AppUser.class));
    }

    @Test
    void loginRejectsUnknownEmailWithoutCheckingPassword() {
        LoginRequest request = new LoginRequest("missing@example.com", "Password1!");
        when(appUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(AppUser.class));
    }
}
