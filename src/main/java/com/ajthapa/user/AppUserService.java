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
        return appUserRepository.findAll().stream().map(
                user -> new AppUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getCreatedAt()
                )
        ).toList();
    }

    //needs to implement security, validation, etc.
    public void insertAppUser(CreateAppUserRequest createAppUserRequest) {
        AppUser user = new AppUser(
                null,
                createAppUserRequest.name(),
                createAppUserRequest.email()
        );

        appUserRepository.save(user);
    }

    public AppUserResponse getUsersById(Long id) {
        return appUserRepository.findById(id).map(user -> new AppUserResponse(
                id,
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        )).orElseThrow(() -> new IllegalArgumentException(id + "not found"));
    }
}
