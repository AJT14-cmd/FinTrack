package com.ajthapa.auth;

import com.ajthapa.account.Account;
import com.ajthapa.account.AccountRepository;
import com.ajthapa.budget.Budget;
import com.ajthapa.budget.BudgetRepository;
import com.ajthapa.category.Category;
import com.ajthapa.category.CategoryRepository;
import com.ajthapa.transaction.Transaction;
import com.ajthapa.transaction.TransactionRepository;
import com.ajthapa.user.AppUser;
import com.ajthapa.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    private static final String PASSWORD = "StrongPass1!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Test
    void registerAndLoginSuccessfullyWithoutExposingPasswordHash() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Alice", "alice@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        AppUser savedUser = appUserRepository.findByEmail("alice@example.com").orElseThrow();
        assertNotEquals(PASSWORD, savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(PASSWORD, savedUser.getPasswordHash()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("alice@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        register("Alice", "alice@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("alice@example.com", "WrongPass1!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void missingTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenAccessesProtectedEndpointWithoutExposingPasswordHash() throws Exception {
        register("Alice", "alice@example.com");
        String token = login("alice@example.com", PASSWORD);

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void userCannotReadUpdateOrDeleteAnotherUsersFinancialData() throws Exception {
        register("Alice", "alice@example.com");
        register("Bob", "bob@example.com");
        String aliceToken = login("alice@example.com", PASSWORD);
        String bobToken = login("bob@example.com", PASSWORD);

        AppUser bob = appUserRepository.findByEmail("bob@example.com").orElseThrow();
        createBobFinancialData(bobToken);

        Account bobAccount = accountRepository.findByAppUserId(bob.getId()).getFirst();
        Category category = categoryRepository.findAll().getFirst();
        Transaction bobTransaction = transactionRepository.findByAccountAppUserId(bob.getId()).getFirst();
        Budget bobBudget = budgetRepository.findByAppUserId(bob.getId()).getFirst();

        assertCannotReadUpdateOrDeleteAccount(aliceToken, bobAccount.getId());
        assertCannotReadUpdateOrDeleteTransaction(
                aliceToken, bobTransaction.getId(), bobAccount.getId(), category.getId());
        assertCannotReadUpdateOrDeleteBudget(aliceToken, bobBudget.getId(), category.getId());

        assertTrue(accountRepository.findByIdAndAppUserId(bobAccount.getId(), bob.getId()).isPresent());
        assertTrue(transactionRepository
                .findByIdAndAccountAppUserId(bobTransaction.getId(), bob.getId()).isPresent());
        assertTrue(budgetRepository.findByIdAndAppUserId(bobBudget.getId(), bob.getId()).isPresent());
        assertEquals("Bob Checking", accountRepository.findById(bobAccount.getId()).orElseThrow().getName());
    }

    private void createBobFinancialData(String token) throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Bob Checking",
                                  "type": "CHECKING",
                                  "balance": 1000.00
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Groceries",
                                  "type": "EXPENSE"
                                }
                                """))
                .andExpect(status().isCreated());

        AppUser bob = appUserRepository.findByEmail("bob@example.com").orElseThrow();
        Long accountId = accountRepository.findByAppUserId(bob.getId()).getFirst().getId();
        Long categoryId = categoryRepository.findAll().getFirst().getId();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson(accountId, categoryId, "Bob groceries")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(budgetJson(categoryId)))
                .andExpect(status().isCreated());
    }

    private void assertCannotReadUpdateOrDeleteAccount(String token, Long accountId) throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", accountId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/accounts/{id}", accountId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Stolen Account",
                                  "type": "SAVINGS",
                                  "balance": 1.00
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/accounts/{id}", accountId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    private void assertCannotReadUpdateOrDeleteTransaction(
            String token, Long transactionId, Long accountId, Long categoryId) throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", transactionId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/transactions/{id}", transactionId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson(accountId, categoryId, "Stolen transaction")))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/transactions/{id}", transactionId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    private void assertCannotReadUpdateOrDeleteBudget(
            String token, Long budgetId, Long categoryId) throws Exception {
        mockMvc.perform(get("/api/budgets/{id}", budgetId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/budgets/{id}", budgetId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(budgetJson(categoryId)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/budgets/{id}", budgetId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    private void register(String name, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(name, email)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private String registerJson(String name, String email) {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(name, email, PASSWORD);
    }

    private String loginJson(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private String transactionJson(Long accountId, Long categoryId, String description) {
        return """
                {
                  "categoryId": %d,
                  "accountId": %d,
                  "description": "%s",
                  "amount": 25.00,
                  "type": "EXPENSE"
                }
                """.formatted(categoryId, accountId, description);
    }

    private String budgetJson(Long categoryId) {
        return """
                {
                  "categoryId": %d,
                  "month": "2026-09",
                  "limitAmount": 400.00
                }
                """.formatted(categoryId);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
