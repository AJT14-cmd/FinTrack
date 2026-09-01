package com.ajthapa.report;

import com.ajthapa.budget.Budget;
import com.ajthapa.budget.BudgetRepository;
import com.ajthapa.budget.BudgetStatus;
import com.ajthapa.budget.BudgetStatusResponse;
import com.ajthapa.category.Category;
import com.ajthapa.category.CategorySpendingResponse;
import com.ajthapa.transaction.Transaction;
import com.ajthapa.transaction.TransactionRepository;
import com.ajthapa.transaction.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public ReportService(TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public MonthlySummaryResponse getMonthlySummary(int year, int month) {

        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        BigDecimal totalIncome = transactionRepository
                .findByTypeAndTransactionDateTimeBetween(TransactionType.INCOME, start, end)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> expenseTransactions = transactionRepository
                .findByTypeAndTransactionDateTimeBetween(TransactionType.EXPENSE, start, end);

        BigDecimal totalExpenses = expenseTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        List<CategorySpendingResponse> expensesByCategory = expenseTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory))
                .entrySet()
                .stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    BigDecimal amount = entry.getValue()
                            .stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CategorySpendingResponse(
                            category.getId(),
                            category.getName(),
                            amount
                    );
                })
                .toList();

        return new MonthlySummaryResponse(totalIncome, totalExpenses, netSavings, expensesByCategory);
    }

    public List<BudgetStatusResponse> getBudgetStatus(int year, int month) {
        String budgetMonth = String.format("%d-%02d", year, month);

        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<Budget> budgets = budgetRepository.findByMonth(budgetMonth);

        List<Transaction> expenseTransactions = transactionRepository
                .findByTypeAndTransactionDateTimeBetween(
                        TransactionType.EXPENSE,
                        start,
                        end
                );

        return budgets.stream()
                .map(budget -> {
                    BigDecimal spentAmount = expenseTransactions.stream()
                            .filter(transaction ->
                                    transaction.getCategory().getId()
                                            .equals(budget.getCategory().getId())
                            )
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal remainingAmount = budget.getLimitAmount().subtract(spentAmount);

                    int comparison = spentAmount.compareTo(budget.getLimitAmount());

                    BudgetStatus status;

                    if (comparison < 0) {
                        status = BudgetStatus.UNDER_BUDGET;
                    } else if (comparison == 0) {
                        status = BudgetStatus.AT_BUDGET;
                    } else {
                        status = BudgetStatus.OVER_BUDGET;
                    }

                    return new BudgetStatusResponse(
                            budget.getCategory().getId(),
                            budget.getCategory().getName(),
                            budget.getLimitAmount(),
                            spentAmount,
                            remainingAmount,
                            status
                    );
                })
                .toList();
    }
}
