package com.ajthapa.transaction;

import com.ajthapa.account.*;
import com.ajthapa.auth.JwtService;
import com.ajthapa.category.*;
import com.ajthapa.user.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionFilteringIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private AppUserRepository users;
    @Autowired private AccountRepository accounts;
    @Autowired private CategoryRepository categories;
    @Autowired private TransactionRepository transactions;
    @Autowired private EntityManager entityManager;

    private String token;
    private Account checking;
    private Account otherUsersAccount;
    private Category food;
    private Category otherUsersCategory;
    private long firstId;
    private long lastId;

    @BeforeEach
    void setUp() {
        AppUser alice = users.save(new AppUser("Alice", "filter-alice@example.com", "test-hash"));
        AppUser bob = users.save(new AppUser("Bob", "filter-bob@example.com", "test-hash"));
        token = "Bearer " + jwtService.generateToken(alice);
        checking = accounts.save(new Account(null, "Checking", AccountType.CHECKING, BigDecimal.ZERO, alice));
        Account cash = accounts.save(new Account(null, "Cash", AccountType.CASH, BigDecimal.ZERO, alice));
        otherUsersAccount = accounts.save(new Account(null, "Checking", AccountType.CHECKING, BigDecimal.ZERO, bob));
        food = categories.save(new Category(null, "Food", CategoryType.EXPENSE, alice));
        Category salary = categories.save(new Category(null, "Salary", CategoryType.INCOME, alice));
        otherUsersCategory = categories.save(new Category(null, "Food", CategoryType.EXPENSE, bob));
        firstId = save(checking, food, TransactionType.EXPENSE, "10", "2026-09-02T00:00:00");
        lastId = save(checking, food, TransactionType.EXPENSE, "20", "2026-09-02T23:59:59.999999");
        save(cash, food, TransactionType.EXPENSE, "30", "2026-09-01T23:59:59.999999");
        save(checking, salary, TransactionType.INCOME, "100", "2026-09-03T00:00:00");
        save(otherUsersAccount, otherUsersCategory, TransactionType.EXPENSE, "5", "2026-09-02T12:00:00");
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void filtersAccountAndCategoryIndependently() throws Exception {
        list("?accountId=" + checking.getId()).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
        list("?categoryId=" + food.getId()).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @ParameterizedTest
    @CsvSource({"INCOME,1", "EXPENSE,3"})
    void filtersType(String type, int count) throws Exception {
        list("?type=" + type).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(count));
    }

    @ParameterizedTest
    @CsvSource({
            "?startDate=2026-09-02,3",
            "?endDate=2026-09-02,3",
            "?startDate=2026-09-02&endDate=2026-09-02,2",
            "?startDate=2026-10-01,0"
    })
    void dateBoundsIncludeWholeDaysAndSupportOpenRanges(String query, int count) throws Exception {
        list(query).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(count));
    }

    @Test
    void combinesAllFiltersWithSortingPaginationAndScopedTotals() throws Exception {
        String query = "?accountId=" + checking.getId() + "&categoryId=" + food.getId()
                + "&type=EXPENSE&startDate=2026-09-02&endDate=2026-09-02&size=1&sortBy=amount&direction=asc";
        list(query).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(firstId));
        list(query + "&page=1").andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(lastId));
        list(query + "&page=2").andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void unknownOrOtherUsersResourceFiltersReturnEmptyPages() throws Exception {
        for (String query : new String[]{"?accountId=" + otherUsersAccount.getId(),
                "?categoryId=" + otherUsersCategory.getId(), "?accountId=9223372036854775807",
                "?categoryId=9223372036854775807",
                "?accountId=" + checking.getId() + "&categoryId=" + otherUsersCategory.getId()}) {
            list(query).andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @ParameterizedTest
    @CsvSource({"accountId,0", "categoryId,-1", "accountId,abc", "categoryId,1.5",
            "type,OTHER", "type,expense", "startDate,not-a-date", "endDate,2026-02-30"})
    void rejectsInvalidFilters(String name, String value) throws Exception {
        mockMvc.perform(get("/api/transactions").header("Authorization", token).param(name, value))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsReversedDates() throws Exception {
        list("?startDate=2026-09-03&endDate=2026-09-01").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date must be on or before end date"));
    }

    private ResultActions list(String query) throws Exception {
        return mockMvc.perform(get("/api/transactions" + query).header("Authorization", token));
    }

    private long save(Account account, Category category, TransactionType type, String amount, String date) {
        Transaction transaction = transactions.save(new Transaction(null, category, account,
                "Filter fixture", new BigDecimal(amount), type));
        entityManager.createQuery("update Transaction t set t.transactionDateTime = :date where t.id = :id")
                .setParameter("date", LocalDateTime.parse(date))
                .setParameter("id", transaction.getId()).executeUpdate();
        return transaction.getId();
    }
}
