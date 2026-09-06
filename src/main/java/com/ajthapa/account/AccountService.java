package com.ajthapa.account;

import com.ajthapa.user.AppUser;
import com.ajthapa.user.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AppUserRepository appUserRepository;

    public AccountService(AccountRepository accountRepository, AppUserRepository appUserRepository) {
        this.accountRepository = accountRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::mapResponse).toList();
    }

    public AccountResponse getAccountById(Long id) {
        return accountRepository.findById(id).map(this::mapResponse)
                .orElseThrow(() -> new IllegalStateException("Account " + id + " not found"));
    }

    public AccountResponse createAccount(CreateAccountRequest createAccountRequest) {
        AppUser appUser = appUserRepository.findById(createAccountRequest.appUserId())
                .orElseThrow(() -> new IllegalStateException("User " + createAccountRequest.appUserId() + " not found"));

        Account account = new Account(
                null,
                createAccountRequest.name(),
                createAccountRequest.type(),
                createAccountRequest.balance(),
                appUser
        );

        Account savedAccount = accountRepository.save(account);

        return mapResponse(savedAccount);
    }

    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Account " + id + " not found"));

        accountRepository.delete(account);
    }

    public AccountResponse updateAccount(Long id, UpdateAccountRequest updateAccountRequest) {
        AppUser appUser = appUserRepository.findById(updateAccountRequest.appUserId())
                .orElseThrow(() -> new IllegalStateException("User " + updateAccountRequest.appUserId() + " not found"));

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Account " + id + " not found"));

        account.setName(updateAccountRequest.name());
        account.setType(updateAccountRequest.type());
        account.setBalance(updateAccountRequest.balance());
        account.setAppUser(appUser);

        Account savedAccount = accountRepository.save(account);

        return mapResponse(savedAccount);
    }

    public List<AccountResponse> findByAppUserId(Long appUserId) {
        if (!appUserRepository.existsById(appUserId)) {
            throw new IllegalStateException("User " + appUserId + " not found");
        }

        return accountRepository.findByAppUserId(appUserId).stream()
                .map(this::mapResponse).toList();
    }

    private AccountResponse mapResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getAppUser().getId(),
                account.getAppUser().getName()
        );
    }
}
