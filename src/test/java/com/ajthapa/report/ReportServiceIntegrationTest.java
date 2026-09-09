package com.ajthapa.report;

import com.ajthapa.account.Account;
import com.ajthapa.account.AccountRepository;
import com.ajthapa.account.AccountType;
import com.ajthapa.budget.Budget;
import com.ajthapa.budget.BudgetRepository;
import com.ajthapa.budget.BudgetStatus;
import com.ajthapa.budget.BudgetStatusResponse;
import com.ajthapa.category.Category;
import com.ajthapa.category.CategoryRepository;
import com.ajthapa.category.CategoryType;
import com.ajthapa.transaction.Transaction;
import com.ajthapa.transaction.TransactionRepository;
import com.ajthapa.transaction.TransactionType;
import com.ajthapa.user.AppUser;
import com.ajthapa.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private AppUser requestedUser;
    private AppUser otherUser;
    private Account requestedUserAccount;
    private Account otherUserAccount;
    private Category incomeCategory;
    private Category expenseCategory;
    private YearMonth currentMonth;

    @BeforeEach
    void setUp() {
        requestedUser = appUserRepository.save(new AppUser(
                "Requested User", "requested@example.com", "$2a$10$testHashRequestedUser"));
        otherUser = appUserRepository.save(new AppUser(
                "Other User", "other@example.com", "$2a$10$testHashOtherUser"));

        requestedUserAccount = accountRepository.save(new Account(
                null, "Requested Checking", AccountType.CHECKING, money("1000.00"), requestedUser));
        otherUserAccount = accountRepository.save(new Account(
                null, "Other Checking", AccountType.CHECKING, money("5000.00"), otherUser));

        incomeCategory = categoryRepository.save(new Category(null, "Salary", CategoryType.INCOME));
        expenseCategory = categoryRepository.save(new Category(null, "Groceries", CategoryType.EXPENSE));
        currentMonth = YearMonth.now();
    }

    @Test
    void monthlySummaryIncludesOnlyRequestedUser() {
        saveTransaction(requestedUserAccount, incomeCategory, "1000.00", TransactionType.INCOME);
        saveTransaction(requestedUserAccount, expenseCategory, "125.00", TransactionType.EXPENSE);
        saveTransaction(otherUserAccount, incomeCategory, "5000.00", TransactionType.INCOME);
        saveTransaction(otherUserAccount, expenseCategory, "900.00", TransactionType.EXPENSE);

        MonthlySummaryResponse summary = reportService.getMonthlySummary(
                currentMonth.getYear(), currentMonth.getMonthValue(), requestedUser.getId());

        assertAll(
                () -> assertMoney("1000.00", summary.totalIncome()),
                () -> assertMoney("125.00", summary.totalExpenses()),
                () -> assertMoney("875.00", summary.netSavings()),
                () -> assertEquals(1, summary.categorySpendingResponse().size()),
                () -> assertMoney("125.00", summary.categorySpendingResponse().getFirst().amount())
        );
    }

    @Test
    void budgetStatusIncludesOnlyRequestedUser() {
        String month = currentMonth.toString();
        budgetRepository.save(new Budget(null, expenseCategory, requestedUser, month, money("200.00")));
        budgetRepository.save(new Budget(null, expenseCategory, otherUser, month, money("1000.00")));
        saveTransaction(requestedUserAccount, expenseCategory, "125.00", TransactionType.EXPENSE);
        saveTransaction(otherUserAccount, expenseCategory, "900.00", TransactionType.EXPENSE);

        List<BudgetStatusResponse> statuses = reportService.getBudgetStatus(
                currentMonth.getYear(), currentMonth.getMonthValue(), requestedUser.getId());

        assertEquals(1, statuses.size());
        BudgetStatusResponse status = statuses.getFirst();
        assertAll(
                () -> assertMoney("200.00", status.limitAmount()),
                () -> assertMoney("125.00", status.spentAmount()),
                () -> assertMoney("75.00", status.remainingAmount()),
                () -> assertEquals(BudgetStatus.UNDER_BUDGET, status.status())
        );
    }

    private void saveTransaction(Account account, Category category, String amount, TransactionType type) {
        transactionRepository.save(new Transaction(
                null,
                category,
                account,
                "Test transaction",
                money(amount),
                type
        ));
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, money(expected).compareTo(actual));
    }

    private BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }
}
