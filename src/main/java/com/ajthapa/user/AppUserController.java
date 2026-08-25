package com.ajthapa.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<AppUserResponse> getUsers() {
        return appUserService.getAllUsers();
    }

    @GetMapping("{id}")
    public AppUserResponse getUsersById(@PathVariable Long id) {
        return appUserService.getUsersById(id);
    }

    @PostMapping
    public AppUserResponse addNewUsers(@Valid @RequestBody CreateAppUserRequest createAppUserRequest) {
        return appUserService.insertAppUser(createAppUserRequest);
    }



}
