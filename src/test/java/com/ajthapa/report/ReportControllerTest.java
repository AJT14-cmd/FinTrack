package com.ajthapa.report;

import com.ajthapa.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private static final Long UNKNOWN_USER_ID = 999L;

    @Mock
    private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void monthlySummaryReturnsNotFoundForUnknownUser() throws Exception {
        when(reportService.getMonthlySummary(2026, 9, UNKNOWN_USER_ID))
                .thenThrow(userNotFound());

        mockMvc.perform(get("/api/reports/monthly-summary")
                        .param("year", "2026")
                        .param("month", "9")
                        .param("userId", UNKNOWN_USER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User 999 not found"));
    }

    @Test
    void budgetStatusReturnsNotFoundForUnknownUser() throws Exception {
        when(reportService.getBudgetStatus(2026, 9, UNKNOWN_USER_ID))
                .thenThrow(userNotFound());

        mockMvc.perform(get("/api/reports/budget-status")
                        .param("year", "2026")
                        .param("month", "9")
                        .param("userId", UNKNOWN_USER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User 999 not found"));
    }

    @Test
    void accountBalancesReturnsNotFoundForUnknownUser() throws Exception {
        when(reportService.getAccountBalances(UNKNOWN_USER_ID))
                .thenThrow(userNotFound());

        mockMvc.perform(get("/api/reports/account-balances")
                        .param("userId", UNKNOWN_USER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User 999 not found"));
    }

    private IllegalStateException userNotFound() {
        return new IllegalStateException("User " + UNKNOWN_USER_ID + " not found");
    }
}
