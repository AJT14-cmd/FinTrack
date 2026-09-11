package com.ajthapa.report;

import com.ajthapa.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private static final Long UNKNOWN_USER_ID = 999L;
    private static final Jwt JWT = Jwt.withTokenValue("token")
            .header("alg", "HS256")
            .subject(UNKNOWN_USER_ID.toString())
            .build();

    @Mock
    private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(jwtArgumentResolver())
                .build();
    }

    @Test
    void monthlySummaryReturnsNotFoundForUnknownUser() throws Exception {
        when(reportService.getMonthlySummary(2026, 9, UNKNOWN_USER_ID))
                .thenThrow(userNotFound());

        mockMvc.perform(get("/api/reports/monthly-summary")
                        .param("year", "2026")
                        .param("month", "9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User 999 not found"));
    }

    @Test
    void budgetStatusReturnsNotFoundForUnknownUser() throws Exception {
        when(reportService.getBudgetStatus(2026, 9, UNKNOWN_USER_ID))
                .thenThrow(userNotFound());

        mockMvc.perform(get("/api/reports/budget-status")
                        .param("year", "2026")
                        .param("month", "9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User 999 not found"));
    }

    @Test
    void accountBalancesReturnsNotFoundForUnknownUser() throws Exception {
        when(reportService.getAccountBalances(UNKNOWN_USER_ID))
                .thenThrow(userNotFound());

        mockMvc.perform(get("/api/reports/account-balances"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User 999 not found"));
    }

    private HandlerMethodArgumentResolver jwtArgumentResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && Jwt.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return JWT;
            }
        };
    }

    private IllegalStateException userNotFound() {
        return new IllegalStateException("User " + UNKNOWN_USER_ID + " not found");
    }
}
