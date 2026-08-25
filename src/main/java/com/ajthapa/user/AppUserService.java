package com.ajthapa.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<AppUserResponse> getAllUsers() {
        return appUserRepository.findAll().stream().map(this::mapResponse).toList();
    }

    public AppUserResponse insertAppUser(CreateAppUserRequest createAppUserRequest) {
        AppUser user = new AppUser(
                null,
                createAppUserRequest.name(),
                createAppUserRequest.email()
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
