package com.ajthapa.account;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> getAccounts(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return accountService.getAccounts(userId);
    }

    @GetMapping("{id}")
    public AccountResponse getAccountById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return accountService.getAccountById(id, Long.valueOf(jwt.getSubject()));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest,
                                                         @AuthenticationPrincipal Jwt jwt) {
        AccountResponse accountResponse = accountService.createAccount(Long.valueOf(jwt.getSubject()), createAccountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        accountService.deleteAccount(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateAccountRequest updateAccountRequest,
                                                         @AuthenticationPrincipal Jwt jwt) {
        AccountResponse accountResponse = accountService.updateAccount(id, updateAccountRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(accountResponse);
    }
}
