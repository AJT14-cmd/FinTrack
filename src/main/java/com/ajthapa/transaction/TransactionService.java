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
        return transactionRepository.findAll().stream().map(
                transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getAccountId(),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getTransactionDateTime()
                )).toList();
    }

    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id).map(
                transaction -> new TransactionResponse(
                        id,
                        transaction.getAccountId(),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getTransactionDateTime()
                )).orElseThrow(() -> new IllegalStateException(id + "not found")
        );
    }

    public void insertTransaction(CreateTransactionRequest createTransactionRequest) {

        Transaction transaction = new Transaction(
                null,
                createTransactionRequest.accountId(),
                createTransactionRequest.description(),
                createTransactionRequest.amount(),
                createTransactionRequest.type()
        );

        transactionRepository.save(transaction);
    }
}
