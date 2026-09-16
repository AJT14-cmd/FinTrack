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
class TransactionPaginationIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private AppUserRepository users;
    @Autowired private AccountRepository accounts;
    @Autowired private CategoryRepository categories;
    @Autowired private TransactionRepository transactions;
    @Autowired private EntityManager entityManager;

    private String aliceToken;
    private String emptyUserToken;
    private long firstId;
    private long secondId;
    private long thirdId;

    @BeforeEach
    void setUp() {
        AppUser alice = users.save(new AppUser("Alice", "page-alice@example.com", "unused-test-hash"));
        AppUser bob = users.save(new AppUser("Bob", "page-bob@example.com", "unused-test-hash"));
        AppUser emptyUser = users.save(new AppUser("Empty", "page-empty@example.com", "unused-test-hash"));
        aliceToken = "Bearer " + jwtService.generateToken(alice);
        emptyUserToken = "Bearer " + jwtService.generateToken(emptyUser);
        Account aliceAccount = accounts.save(new Account(null, "Checking", AccountType.CHECKING,
                BigDecimal.ZERO, alice));
        Account bobAccount = accounts.save(new Account(null, "Checking", AccountType.CHECKING,
                BigDecimal.ZERO, bob));
        Category aliceCategory = categories.save(new Category(null, "Food", CategoryType.EXPENSE, alice));
        Category bobCategory = categories.save(new Category(null, "Food", CategoryType.EXPENSE, bob));

        firstId = saveTransaction(aliceAccount, aliceCategory, "30.00", "2026-09-01T12:00:00");
        saveTransaction(bobAccount, bobCategory, "1.00", "2026-09-03T12:00:00");
        secondId = saveTransaction(aliceAccount, aliceCategory, "10.00", "2026-09-02T12:00:00");
        thirdId = saveTransaction(aliceAccount, aliceCategory, "10.00", "2026-09-02T12:00:00");
        saveTransaction(bobAccount, bobCategory, "99.00", "2026-09-04T12:00:00");
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void defaultsReturnOnlyCurrentUsersTransactionsWithStableDateOrdering() throws Exception {
        list("").andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].id").value(thirdId))
                .andExpect(jsonPath("$.content[1].id").value(secondId))
                .andExpect(jsonPath("$.content[2].id").value(firstId));
    }

    @Test
    void pagesDoNotOverlapAndTotalsExcludeOtherUsers() throws Exception {
        list("?page=0&size=2").andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(thirdId))
                .andExpect(jsonPath("$.content[1].id").value(secondId))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
        list("?page=1&size=2").andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(firstId))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
        list("?page=2&size=2").andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @ParameterizedTest
    @CsvSource({
            "amount,asc,3,2,1", "amount,desc,1,3,2",
            "id,asc,1,2,3", "id,desc,3,2,1",
            "transactionDateTime,asc,1,3,2", "transactionDateTime,DESC,3,2,1"
    })
    void sortsByAllowedFieldsWithIdTieBreaker(String field, String direction,
                                             int first, int second, int third) throws Exception {
        long[] ids = {firstId, secondId, thirdId};
        list("?sortBy=" + field + "&direction=" + direction).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].id").value(ids[first - 1]))
                .andExpect(jsonPath("$.content[1].id").value(ids[second - 1]))
                .andExpect(jsonPath("$.content[2].id").value(ids[third - 1]));
    }

    @Test
    void userWithoutTransactionsGetsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/transactions").header("Authorization", emptyUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @ParameterizedTest
    @CsvSource({"1,3", "100,1"})
    void acceptsPageSizeBoundaries(int size, int totalPages) throws Exception {
        list("?size=" + size).andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.content.length()").value(Math.min(size, 3)))
                .andExpect(jsonPath("$.totalPages").value(totalPages));
    }

    @ParameterizedTest
    @CsvSource({
            "page,-1,Page cannot be negative",
            "size,0,Size must be between 1 and 100",
            "size,-1,Size must be between 1 and 100",
            "size,101,Size must be between 1 and 100",
            "sortBy,account.appUser.id,Unsupported sort field: account.appUser.id",
            "direction,sideways,Sorting direction must be either asc or desc",
            "page,2147483647,Page offset is too large"
    })
    void invalidParametersReturnBadRequest(String parameter, String value, String message) throws Exception {
        mockMvc.perform(get("/api/transactions").header("Authorization", aliceToken).param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    @ParameterizedTest
    @CsvSource({"page,abc", "size,abc", "page,2147483648", "size,1.5"})
    void malformedNumbersReturnBadRequest(String parameter, String value) throws Exception {
        mockMvc.perform(get("/api/transactions").header("Authorization", aliceToken).param(parameter, value))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions").param("size", "2"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions list(String query) throws Exception {
        return mockMvc.perform(get("/api/transactions" + query).header("Authorization", aliceToken));
    }

    private long saveTransaction(Account account, Category category, String amount, String date) {
        Transaction transaction = transactions.save(new Transaction(null, category, account,
                "Pagination fixture", new BigDecimal(amount), TransactionType.EXPENSE));
        // Fixed timestamps exercise ties without depending on clock resolution.
        entityManager.createQuery("update Transaction t set t.transactionDateTime = :date where t.id = :id")
                .setParameter("date", LocalDateTime.parse(date))
                .setParameter("id", transaction.getId()).executeUpdate();
        return transaction.getId();
    }
}
