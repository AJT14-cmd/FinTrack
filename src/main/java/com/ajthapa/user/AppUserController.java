package com.ajthapa.user;

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

    // change this, dont accept entities
    @PostMapping
    public void addNewUsers(@RequestBody CreateAppUserRequest createAppUserRequest) {
        appUserService.insertAppUser(createAppUserRequest);
    }



}
