package com.ajthapa.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/accounts")
@Tag(name = "Accounts", description = "Manage the authenticated user's financial accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "List the authenticated user's accounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping
    public List<AccountResponse> getAccounts(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return accountService.getAccounts(userId);
    }

    @Operation(summary = "Get an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("{id}")
    public AccountResponse getAccountById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return accountService.getAccountById(id, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Create an account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest,
                                                         @AuthenticationPrincipal Jwt jwt) {
        AccountResponse accountResponse = accountService.createAccount(Long.valueOf(jwt.getSubject()), createAccountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @Operation(summary = "Delete an account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        accountService.deleteAccount(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Account or user not found")
    })
    @PutMapping("{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateAccountRequest updateAccountRequest,
                                                         @AuthenticationPrincipal Jwt jwt) {
        AccountResponse accountResponse = accountService.updateAccount(id, updateAccountRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(accountResponse);
    }
}
