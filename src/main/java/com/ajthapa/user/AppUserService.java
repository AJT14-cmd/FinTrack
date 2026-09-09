package com.ajthapa.user;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;

@Service
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUserResponse> getAllUsers() {
        return appUserRepository.findAll().stream().map(this::mapResponse).toList();
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

        appUserRepository.save(user);

        return mapResponse(user);
    }

    public AppUserResponse getUsersById(Long id) {
        return appUserRepository.findById(id).map(this::mapResponse).
                orElseThrow(() -> new IllegalStateException(id + " not found"));
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
