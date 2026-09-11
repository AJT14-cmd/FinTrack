package com.ajthapa.transaction;
import com.ajthapa.account.Account;
import com.ajthapa.account.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.ajthapa.category.Category;
import com.ajthapa.category.CategoryRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
                              AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }

    public List<TransactionResponse> getAllTransactions(Long userId) {
        return transactionRepository.findByAccountAppUserId(userId).stream().map(this::mapResponse).toList();
    }

    public TransactionResponse getTransactionById(Long id, Long userId) {
        return transactionRepository.findByIdAndAccountAppUserId(id, userId).map(this::mapResponse).orElseThrow(() ->
                new IllegalStateException(id + " not found"));
    }

    @Transactional
    public TransactionResponse insertTransaction(CreateTransactionRequest createTransactionRequest, Long userId) {
        Category category = categoryRepository.findById(createTransactionRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + createTransactionRequest.categoryId() + " not found"));

        Account account = accountRepository.findByIdAndAppUserId(createTransactionRequest.accountId(), userId)
                .orElseThrow(() -> new IllegalStateException("Account " + createTransactionRequest.accountId() + " not found"));

        if (createTransactionRequest.type() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(createTransactionRequest.amount()));
        } else if (createTransactionRequest.type() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(createTransactionRequest.amount()));
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction(
                null,
                category,
                account,
                createTransactionRequest.description(),
                createTransactionRequest.amount(),
                createTransactionRequest.type()
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapResponse(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndAccountAppUserId(id, userId)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        Account account = transaction.getAccount();

        if (transaction.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        accountRepository.save(account);

        transactionRepository.delete(transaction);

    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest updateTransactionRequest, Long userId) {
        Category category = categoryRepository.findById(updateTransactionRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + updateTransactionRequest.categoryId() + " not found"));

        Account newAccount = accountRepository.findByIdAndAppUserId(updateTransactionRequest.accountId(), userId)
                .orElseThrow(() -> new IllegalStateException("Account " + updateTransactionRequest.accountId() + " not found"));

        Transaction transaction = transactionRepository.findByIdAndAccountAppUserId(id, userId)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        Account oldAccount = transaction.getAccount();

        if (!oldAccount.getAppUser().getId().equals(newAccount.getAppUser().getId())) {
            throw new IllegalStateException("Transaction cannot be moved to another user's account");
        }

        if (transaction.getType() == TransactionType.INCOME) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(transaction.getAmount()));
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            oldAccount.setBalance(oldAccount.getBalance().add(transaction.getAmount()));
        }

        if (updateTransactionRequest.type() == TransactionType.INCOME) {
            newAccount.setBalance(newAccount.getBalance().add(updateTransactionRequest.amount()));
        } else if (updateTransactionRequest.type() == TransactionType.EXPENSE) {
            newAccount.setBalance(newAccount.getBalance().subtract(updateTransactionRequest.amount()));
        }

        transaction.setCategory(category);
        transaction.setAccount(newAccount);
        transaction.setDescription(updateTransactionRequest.description());
        transaction.setAmount(updateTransactionRequest.amount());
        transaction.setType(updateTransactionRequest.type());

        accountRepository.save(oldAccount);
        accountRepository.save(newAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapResponse(savedTransaction);
    }

    private TransactionResponse mapResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getAccount().getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDateTime()
        );
    }
}
