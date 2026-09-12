package com.ajthapa.budget;

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
@RequestMapping("api/budgets")
@Tag(name = "Budgets", description = "Manage the authenticated user's monthly budgets")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Operation(summary = "List the authenticated user's budgets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budgets returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping
    public List<BudgetResponse> getAllBudgets(@AuthenticationPrincipal Jwt jwt) {
        return budgetService.getAllBudgets(Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Get a budget")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @GetMapping("{id}")
    public BudgetResponse getBudgetById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return budgetService.getBudgetById(id, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Create a budget")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Budget created"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "User or category not found")
    })
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody CreateBudgetRequest createBudgetRequest,
                                                       @AuthenticationPrincipal Jwt jwt) {
        BudgetResponse budgetResponse = budgetService.createBudget(createBudgetRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetResponse);
    }

    @Operation(summary = "Delete a budget")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Budget deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        budgetService.deleteBudget(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a budget")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget updated"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Budget, user, or category not found")
    })
    @PutMapping("{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateBudgetRequest updateBudgetRequest,
                                                       @AuthenticationPrincipal Jwt jwt) {
        BudgetResponse budgetResponse = budgetService.updateBudget(id, updateBudgetRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(budgetResponse);
    }
}
