package com.ajthapa.report;

import com.ajthapa.account.AccountBalanceResponse;
import com.ajthapa.budget.BudgetStatusResponse;
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
                                                    @RequestParam Long userId) {
        return reportService.getMonthlySummary(year, month, userId);
    }

    @GetMapping("budget-status")
    public List<BudgetStatusResponse> getBudgetStatus(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam Long userId
    ) {
        return reportService.getBudgetStatus(year, month, userId);
    }

    @GetMapping("/account-balances")
    public List<AccountBalanceResponse> getAccountBalances(@RequestParam Long userId) {
        return reportService.getAccountBalances(userId);
    }
}
