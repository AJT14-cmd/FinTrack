package com.ajthapa.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> addNewUsers(@Valid @RequestBody CreateAppUserRequest createAppUserRequest) {
        AppUserResponse appUserResponse =  appUserService.insertAppUser(createAppUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(appUserResponse);
    }



}
