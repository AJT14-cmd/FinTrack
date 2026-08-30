package com.ajthapa.transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import com.ajthapa.category.Category;
import com.ajthapa.category.CategoryRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream().map(this::mapResponse).toList();
    }

    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id).map(this::mapResponse).orElseThrow(() ->
                new IllegalStateException(id + " not found"));
    }

    public TransactionResponse insertTransaction(CreateTransactionRequest createTransactionRequest) {
        Category category = categoryRepository.findById(createTransactionRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + createTransactionRequest.categoryId() + " not found"));

        Transaction transaction = new Transaction(
                null,
                category,
                createTransactionRequest.accountId(),
                createTransactionRequest.description(),
                createTransactionRequest.amount(),
                createTransactionRequest.type()
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapResponse(savedTransaction);
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        transactionRepository.delete(transaction);

    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest updateTransactionRequest) {
        Category category = categoryRepository.findById(updateTransactionRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + updateTransactionRequest.categoryId() + " not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        transaction.setCategory(category);
        transaction.setAccountId(updateTransactionRequest.accountId());
        transaction.setDescription(updateTransactionRequest.description());
        transaction.setAmount(updateTransactionRequest.amount());
        transaction.setType(updateTransactionRequest.type());

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapResponse(savedTransaction);
    }

    private TransactionResponse mapResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getAccountId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDateTime()
        );
    }
}
