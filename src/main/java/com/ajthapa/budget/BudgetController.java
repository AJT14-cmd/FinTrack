package com.ajthapa.budget;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public List<BudgetResponse> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @GetMapping("{id}")
    public BudgetResponse getBudgetById(@PathVariable Long id) {
        return budgetService.getBudgetById(id);
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody CreateBudgetRequest createBudgetRequest) {
        BudgetResponse budgetResponse = budgetService.createBudget(createBudgetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateBudgetRequest updateBudgetRequest) {
        BudgetResponse budgetResponse = budgetService.updateBudget(id, updateBudgetRequest);
        return ResponseEntity.ok(budgetResponse);
    }
}
