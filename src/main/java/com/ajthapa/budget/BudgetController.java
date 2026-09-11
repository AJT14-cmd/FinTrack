package com.ajthapa.budget;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> getAllBudgets(@AuthenticationPrincipal Jwt jwt) {
        return budgetService.getAllBudgets(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("{id}")
    public BudgetResponse getBudgetById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return budgetService.getBudgetById(id, Long.valueOf(jwt.getSubject()));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody CreateBudgetRequest createBudgetRequest,
                                                       @AuthenticationPrincipal Jwt jwt) {
        BudgetResponse budgetResponse = budgetService.createBudget(createBudgetRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        budgetService.deleteBudget(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateBudgetRequest updateBudgetRequest,
                                                       @AuthenticationPrincipal Jwt jwt) {
        BudgetResponse budgetResponse = budgetService.updateBudget(id, updateBudgetRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(budgetResponse);
    }
}
