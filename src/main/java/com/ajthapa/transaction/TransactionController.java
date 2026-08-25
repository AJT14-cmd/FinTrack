package com.ajthapa.transaction;

import jakarta.validation.Valid;
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
    public List<TransactionResponse> getTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("{id}")
    public TransactionResponse getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @PostMapping
    public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest createTransactionRequest) {
        return transactionService.insertTransaction(createTransactionRequest);
    }

    @DeleteMapping("{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}
