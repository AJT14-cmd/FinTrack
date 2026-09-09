package com.ajthapa.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

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

        AppUserResponse response = appUserService.register(request);

        assertEquals("Test User", response.name());
        assertEquals("test.user@example.com", response.email());
        verify(passwordEncoder).encode("Password1!");
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void registrationRejectsDuplicateEmailBeforeHashingOrSaving() {
        RegisterRequest request = new RegisterRequest(
                "Test User", "test@example.com", "Password1!");
        AppUser existingUser = new AppUser("Existing User", "test@example.com", "encoded-password");
        when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> appUserService.register(request)
        );

        assertEquals("A user with email test@example.com already exists", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }
}
