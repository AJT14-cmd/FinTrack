package com.ajthapa.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategorySecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String aliceToken;
    private String bobToken;
    private long aliceCategoryId;
    private long bobCategoryId;
    private long aliceAccountId;

    @BeforeEach
    void setUp() throws Exception {
        aliceToken = registerAndLogin("category-alice@example.com");
        bobToken = registerAndLogin("category-bob@example.com");
        aliceCategoryId = create("/api/categories", aliceToken, categoryJson("Alice food"));
        bobCategoryId = create("/api/categories", bobToken, categoryJson("Bob food"));
        aliceAccountId = create("/api/accounts", aliceToken, """
                {"name":"Alice checking","type":"CHECKING","balance":1000.00}
                """);
    }

    @Test
    void categoryListContainsOnlyCurrentUsersCategories() throws Exception {
        mockMvc.perform(get("/api/categories").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(aliceCategoryId));
        mockMvc.perform(get("/api/categories").header("Authorization", bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bobCategoryId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void cannotReadUpdateOrDeleteAnotherUsersCategory(String method) throws Exception {
        MockHttpServletRequestBuilder request = switch (method) {
            case "GET" -> get("/api/categories/{id}", bobCategoryId);
            case "PUT" -> put("/api/categories/{id}", bobCategoryId)
                    .contentType(MediaType.APPLICATION_JSON).content(categoryJson("Changed name"));
            case "DELETE" -> delete("/api/categories/{id}", bobCategoryId);
            default -> throw new IllegalArgumentException(method);
        };
        mockMvc.perform(request.header("Authorization", aliceToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/categories/{id}", bobCategoryId).header("Authorization", bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void ownerCanReadUpdateAndDeleteOwnCategory() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", aliceCategoryId).header("Authorization", aliceToken))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/categories/{id}", aliceCategoryId)
                        .header("Authorization", aliceToken).contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Renamed food")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed food"));
        mockMvc.perform(delete("/api/categories/{id}", aliceCategoryId).header("Authorization", aliceToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/categories/{id}", aliceCategoryId).header("Authorization", aliceToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannotCreateTransactionUsingAnotherUsersCategory() throws Exception {
        mockMvc.perform(post("/api/transactions").header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(transactionJson(bobCategoryId, 75)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/transactions").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        assertBalance(1000);
    }

    @Test
    void cannotUpdateOwnTransactionToUseAnotherUsersCategory() throws Exception {
        long id = create("/api/transactions", aliceToken, transactionJson(aliceCategoryId, 25));
        assertBalance(975);

        mockMvc.perform(put("/api/transactions/{id}", id).header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(transactionJson(bobCategoryId, 75)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/transactions/{id}", id).header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(aliceCategoryId))
                .andExpect(jsonPath("$.accountId").value(aliceAccountId))
                .andExpect(jsonPath("$.amount").value(25.0));
        mockMvc.perform(get("/api/transactions").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        assertBalance(975);
    }

    @Test
    void cannotCreateBudgetUsingAnotherUsersCategory() throws Exception {
        mockMvc.perform(post("/api/budgets").header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(budgetJson(bobCategoryId, 500)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/budgets").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void cannotUpdateOwnBudgetToUseAnotherUsersCategory() throws Exception {
        long id = create("/api/budgets", aliceToken, budgetJson(aliceCategoryId, 400));
        mockMvc.perform(put("/api/budgets/{id}", id).header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(budgetJson(bobCategoryId, 500)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/budgets/{id}", id).header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(aliceCategoryId))
                .andExpect(jsonPath("$.month").value("2026-09"))
                .andExpect(jsonPath("$.limitAmount").value(400.0));
        mockMvc.perform(get("/api/budgets").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private void assertBalance(int expected) throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", aliceAccountId).header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value((double) expected));
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Category tester","email":"%s","password":"StrongPass1!"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"StrongPass1!"}
                                """.formatted(email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).get("token").asText();
    }

    private long create(String path, String token, String body) throws Exception {
        String response = mockMvc.perform(post(path).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String categoryJson(String name) {
        return """
                {"name":"%s","type":"EXPENSE"}
                """.formatted(name);
    }

    private String transactionJson(long categoryId, int amount) {
        return """
                {"categoryId":%d,"accountId":%d,"description":"Groceries","amount":%d,"type":"EXPENSE"}
                """.formatted(categoryId, aliceAccountId, amount);
    }

    private String budgetJson(long categoryId, int limit) {
        return """
                {"categoryId":%d,"month":"2026-09","limitAmount":%d}
                """.formatted(categoryId, limit);
    }
}
