package com.ajthapa.report;

import com.ajthapa.account.AccountBalanceResponse;
import com.ajthapa.budget.BudgetStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/reports")
@Tag(name = "Reports", description = "View financial summaries for the authenticated user")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Get a monthly income and expense summary")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly summary returned"),
            @ApiResponse(responseCode = "400", description = "Missing or malformed query parameter"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("monthly-summary")
    public MonthlySummaryResponse getMonthlySummary(@RequestParam int year,
                                                    @RequestParam int month,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return reportService.getMonthlySummary(year, month, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Compare monthly spending with budgets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget status returned"),
            @ApiResponse(responseCode = "400", description = "Missing or malformed query parameter"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("budget-status")
    public List<BudgetStatusResponse> getBudgetStatus(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return reportService.getBudgetStatus(year, month, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Get current account balances")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account balances returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/account-balances")
    public List<AccountBalanceResponse> getAccountBalances(@AuthenticationPrincipal Jwt jwt) {
        return reportService.getAccountBalances(Long.valueOf(jwt.getSubject()));
    }
}
