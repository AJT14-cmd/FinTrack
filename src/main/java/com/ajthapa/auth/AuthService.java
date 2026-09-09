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
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.email().trim().toLowerCase(Locale.ROOT);

        AppUser user = appUserRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds());
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
