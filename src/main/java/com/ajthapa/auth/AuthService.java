package com.ajthapa.auth;

import com.ajthapa.user.AppUser;
import com.ajthapa.user.AppUserRepository;
import com.ajthapa.user.AppUserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUserResponse register(RegisterRequest registerRequest) {
        String email = registerRequest.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }

        String passwordHash = passwordEncoder.encode(registerRequest.password());

        AppUser user = new AppUser(
                registerRequest.name().trim(),
                email,
                passwordHash
        );

        AppUser savedUser = appUserRepository.save(user);

        return mapResponse(savedUser);
    }

    private AppUserResponse mapResponse(AppUser appUser) {
        return new AppUserResponse(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),
                appUser.getCreatedAt()
        );
    }
}
