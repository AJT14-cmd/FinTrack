package com.ajthapa.transaction;

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
@RequestMapping("api/transactions")
@Tag(name = "Transactions", description = "Manage the authenticated user's income and expenses")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "List the authenticated user's transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping
    public List<TransactionResponse> getTransactions(@AuthenticationPrincipal Jwt jwt) {
        return transactionService.getAllTransactions(Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Get a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("{id}")
    public TransactionResponse getTransactionById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return transactionService.getTransactionById(id, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Create a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Account or category not found")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest createTransactionRequest,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        TransactionResponse transactionResponse = transactionService
                .insertTransaction(createTransactionRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponse);
    }

    @Operation(summary = "Delete a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaction deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        transactionService.deleteTransaction(id, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction updated"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Transaction, account, or category not found")
    })
    @PutMapping("{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateTransactionRequest updateTransactionRequest,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        TransactionResponse transactionResponse = transactionService.updateTransaction(id, updateTransactionRequest, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.ok(transactionResponse);
    }
}
