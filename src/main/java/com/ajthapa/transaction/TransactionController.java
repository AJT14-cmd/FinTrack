package com.ajthapa.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

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
    public TransactionPageResponse getTransactions(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(defaultValue = "transactionDateTime") String sortBy,
                                                     @RequestParam(defaultValue = "desc") String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }

        if ((long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset is too large");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        Set<String> allowedSortFields = Set.of("transactionDateTime", "amount", "id");

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported sort field: " + sortBy);
        }

        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Sorting direction must be either asc or desc");
        }

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sort = Sort.by(sortDirection, sortBy).and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponse> result = transactionService
                .getAllTransactions(Long.valueOf(jwt.getSubject()), pageable);

        return new TransactionPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
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

    @Operation(summary = "Create a transaction", description = "The account and category must belong to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Account or category does not exist or belongs to another user")
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

    @Operation(summary = "Update a transaction", description = "The transaction, account, and category must belong to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction updated"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Transaction, account, or category does not exist or belongs to another user")
    })
    @PutMapping("{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateTransactionRequest updateTransactionRequest,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        TransactionResponse transactionResponse = transactionService.updateTransaction(id, updateTransactionRequest, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.ok(transactionResponse);
    }
}
