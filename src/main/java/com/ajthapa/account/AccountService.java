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

    public List<AccountResponse> getAccounts(Long userId) {
        return accountRepository.findByAppUserId(userId).stream().map(this::mapResponse).toList();
    }

    public AccountResponse getAccountById(Long id, Long userId) {
        return accountRepository.findByIdAndAppUserId(id, userId).map(this::mapResponse)
                .orElseThrow(() -> new IllegalStateException("Account " + id + " not found"));
    }

    public AccountResponse createAccount(Long userId, CreateAccountRequest createAccountRequest) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User " + userId + " not found"));

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

    public void deleteAccount(Long id, Long userId) {
        Account account = accountRepository.findByIdAndAppUserId(id, userId)
                .orElseThrow(() -> new IllegalStateException("Account " + id + " not found"));

        accountRepository.delete(account);
    }

    public AccountResponse updateAccount(Long id, UpdateAccountRequest updateAccountRequest, Long userId) {
        appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User " + userId + " not found"));

        Account account = accountRepository.findByIdAndAppUserId(id, userId)
                .orElseThrow(() -> new IllegalStateException("Account " + id + " not found"));

        account.setName(updateAccountRequest.name());
        account.setType(updateAccountRequest.type());
        account.setBalance(updateAccountRequest.balance());

        Account savedAccount = accountRepository.save(account);

        return mapResponse(savedAccount);
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
