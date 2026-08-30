package com.ajthapa.report;

import com.ajthapa.transaction.Transaction;
import com.ajthapa.transaction.TransactionRepository;
import com.ajthapa.transaction.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MonthlySummaryResponse getMonthlySummary() {
        BigDecimal totalIncome = transactionRepository.findByType(TransactionType.INCOME)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactionRepository.findByType(TransactionType.EXPENSE)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return new MonthlySummaryResponse(totalIncome, totalExpenses, netSavings);
    }
}
