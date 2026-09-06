package com.ajthapa.transaction;
import com.ajthapa.account.Account;
import com.ajthapa.account.AccountRepository;
import com.ajthapa.user.AppUserRepository;
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
    private final AppUserRepository appUserRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
                              AccountRepository accountRepository, AppUserRepository appUserRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream().map(this::mapResponse).toList();
    }

    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id).map(this::mapResponse).orElseThrow(() ->
                new IllegalStateException(id + " not found"));
    }

    @Transactional
    public TransactionResponse insertTransaction(CreateTransactionRequest createTransactionRequest) {
        Category category = categoryRepository.findById(createTransactionRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + createTransactionRequest.categoryId() + " not found"));

        Account account = accountRepository.findById(createTransactionRequest.accountId())
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
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
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
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest updateTransactionRequest) {
        Category category = categoryRepository.findById(updateTransactionRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + updateTransactionRequest.categoryId() + " not found"));

        Account newAccount = accountRepository.findById(updateTransactionRequest.accountId())
                .orElseThrow(() -> new IllegalStateException("Account " + updateTransactionRequest.accountId() + " not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        Account oldAccount = transaction.getAccount();

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

    public List<TransactionResponse> findByAppUserId(Long appUserId) {
        if (!appUserRepository.existsById(appUserId)) {
            throw new IllegalStateException("User " + appUserId + " not found");
        }

        return transactionRepository.findByAccountAppUserId(appUserId).stream()
                .map(this::mapResponse)
                .toList();
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
