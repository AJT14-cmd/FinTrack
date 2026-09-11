package com.ajthapa.transaction;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> getTransactions(@AuthenticationPrincipal Jwt jwt) {
        return transactionService.getAllTransactions(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("{id}")
    public TransactionResponse getTransactionById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return transactionService.getTransactionById(id, Long.valueOf(jwt.getSubject()));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest createTransactionRequest,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        TransactionResponse transactionResponse = transactionService
                .insertTransaction(createTransactionRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        transactionService.deleteTransaction(id, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateTransactionRequest updateTransactionRequest,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        TransactionResponse transactionResponse = transactionService.updateTransaction(id, updateTransactionRequest, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.ok(transactionResponse);
    }
}
