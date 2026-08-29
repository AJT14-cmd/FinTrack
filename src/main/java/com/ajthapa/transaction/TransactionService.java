package com.ajthapa.transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream().map(this::mapResponse).toList();
    }

    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id).map(this::mapResponse).orElseThrow(() ->
                new IllegalStateException(id + " not found"));
    }

    public TransactionResponse insertTransaction(CreateTransactionRequest createTransactionRequest) {

        Transaction transaction = new Transaction(
                null,
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

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest updateTranactionRequest) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + "notfound"));

        transaction.setAccountId(updateTranactionRequest.accountId());
        transaction.setDescription(updateTranactionRequest.description());
        transaction.setAmount(updateTranactionRequest.amount());
        transaction.setType(updateTranactionRequest.type());

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapResponse(savedTransaction);
    }

    private TransactionResponse mapResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDateTime()
        );
    }
}
