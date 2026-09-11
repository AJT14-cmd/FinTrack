package com.ajthapa.report;

import com.ajthapa.account.AccountBalanceResponse;
import com.ajthapa.budget.BudgetStatusResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("monthly-summary")
    public MonthlySummaryResponse getMonthlySummary(@RequestParam int year,
                                                    @RequestParam int month,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return reportService.getMonthlySummary(year, month, Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("budget-status")
    public List<BudgetStatusResponse> getBudgetStatus(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return reportService.getBudgetStatus(year, month, Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/account-balances")
    public List<AccountBalanceResponse> getAccountBalances(@AuthenticationPrincipal Jwt jwt) {
        return reportService.getAccountBalances(Long.valueOf(jwt.getSubject()));
    }
}
