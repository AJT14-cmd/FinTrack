package com.ajthapa.report;

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

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
}
