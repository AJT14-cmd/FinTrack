package com.ajthapa.transaction;

import com.ajthapa.account.Account;
import com.ajthapa.account.AccountRepository;
import com.ajthapa.account.AccountType;
import com.ajthapa.category.Category;
import com.ajthapa.category.CategoryRepository;
import com.ajthapa.category.CategoryType;
import com.ajthapa.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Long CATEGORY_ID = 1L;
    private static final Long ACCOUNT_ID = 10L;
    private static final Long TRANSACTION_ID = 100L;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void creatingIncomeIncreasesAccountBalance() {
        Category category = category(CategoryType.INCOME);
        Account account = account(ACCOUNT_ID, "100.00");
        CreateTransactionRequest request = new CreateTransactionRequest(
                CATEGORY_ID,
                ACCOUNT_ID,
                "Paycheck",
                money("25.00"),
                TransactionType.INCOME
        );

        prepareCreate(category, account);

        transactionService.insertTransaction(request);

        assertEquals(money("125.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void creatingExpenseDecreasesAccountBalance() {
        Category category = category(CategoryType.EXPENSE);
        Account account = account(ACCOUNT_ID, "100.00");
        CreateTransactionRequest request = new CreateTransactionRequest(
                CATEGORY_ID,
                ACCOUNT_ID,
                "Groceries",
                money("25.00"),
                TransactionType.EXPENSE
        );

        prepareCreate(category, account);

        transactionService.insertTransaction(request);

        assertEquals(money("75.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deletingIncomeReversesBalance() {
        Account account = account(ACCOUNT_ID, "125.00");
        Transaction transaction = transaction(account, "25.00", TransactionType.INCOME);
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(TRANSACTION_ID);

        assertEquals(money("100.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deletingExpenseReversesBalance() {
        Account account = account(ACCOUNT_ID, "75.00");
        Transaction transaction = transaction(account, "25.00", TransactionType.EXPENSE);
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(TRANSACTION_ID);

        assertEquals(money("100.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void updatingExpenseAmountChangesBalanceCorrectly() {
        Category category = category(CategoryType.EXPENSE);
        Account account = account(ACCOUNT_ID, "80.00");
        Transaction transaction = transaction(account, "20.00", TransactionType.EXPENSE);
        UpdateTransactionRequest request = new UpdateTransactionRequest(
                CATEGORY_ID,
                ACCOUNT_ID,
                "Updated groceries",
                money("35.00"),
                TransactionType.EXPENSE
        );

        prepareUpdate(category, account, transaction);

        TransactionResponse response = transactionService.updateTransaction(TRANSACTION_ID, request);

        assertAll(
                () -> assertEquals(money("65.00"), account.getBalance()),
                () -> assertEquals(money("35.00"), transaction.getAmount()),
                () -> assertEquals(money("35.00"), response.amount())
        );
    }

    @Test
    void movingTransactionToAnotherAccountUpdatesBothBalances() {
        Long newAccountId = 11L;
        Category category = category(CategoryType.EXPENSE);
        Account oldAccount = account(ACCOUNT_ID, "80.00");
        Account newAccount = account(newAccountId, "200.00");
        Transaction transaction = transaction(oldAccount, "20.00", TransactionType.EXPENSE);
        UpdateTransactionRequest request = new UpdateTransactionRequest(
                CATEGORY_ID,
                newAccountId,
                "Moved groceries",
                money("30.00"),
                TransactionType.EXPENSE
        );

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(accountRepository.findById(newAccountId)).thenReturn(Optional.of(newAccount));
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        transactionService.updateTransaction(TRANSACTION_ID, request);

        assertAll(
                () -> assertEquals(money("100.00"), oldAccount.getBalance()),
                () -> assertEquals(money("170.00"), newAccount.getBalance()),
                () -> assertSame(newAccount, transaction.getAccount())
        );
        verify(accountRepository).save(oldAccount);
        verify(accountRepository).save(newAccount);
    }

    private void prepareCreate(Category category, Account account) {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepareUpdate(Category category, Account account, Transaction transaction) {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);
    }

    private Category category(CategoryType type) {
        return new Category(CATEGORY_ID, type.name(), type);
    }

    private Account account(Long id, String balance) {
        return new Account(id, "Checking", AccountType.CHECKING, money(balance), null);
    }

    private Transaction transaction(Account account, String amount, TransactionType type) {
        CategoryType categoryType = type == TransactionType.INCOME
                ? CategoryType.INCOME
                : CategoryType.EXPENSE;

        return new Transaction(
                TRANSACTION_ID,
                category(categoryType),
                account,
                "Existing transaction",
                money(amount),
                type
        );
    }

    private BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }
}
