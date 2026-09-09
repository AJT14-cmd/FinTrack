package com.ajthapa.user;

import com.ajthapa.account.AccountResponse;
import com.ajthapa.account.AccountService;
import com.ajthapa.budget.BudgetResponse;
import com.ajthapa.budget.BudgetService;
import com.ajthapa.transaction.TransactionResponse;
import com.ajthapa.transaction.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class AppUserController {
    private final AppUserService appUserService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;

    public AppUserController(AppUserService appUserService, AccountService accountService,
                             TransactionService transactionService, BudgetService budgetService) {
        this.appUserService = appUserService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<AppUserResponse> getUsers() {
        return appUserService.getAllUsers();
    }

    @GetMapping("{userId}/accounts")
    public List<AccountResponse> findAccountsByAppUserId(@PathVariable Long userId) {
        return accountService.findByAppUserId(userId);
    }

    @GetMapping("{userId}/transactions")
    public List<TransactionResponse> findTransactionsByAppUserId(@PathVariable Long userId) {
        return transactionService.findByAppUserId(userId);
    }

    @GetMapping("{userId}/budgets")
    public List<BudgetResponse> findBudgetsByAppUserId(@PathVariable Long userId) {
        return budgetService.findByAppUserId(userId);
    }

    @GetMapping("{id}")
    public AppUserResponse getUsersById(@PathVariable Long id) {
        return appUserService.getUsersById(id);
    }
}
