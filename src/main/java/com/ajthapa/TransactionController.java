package com.ajthapa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("transaction")
public class TransactionController {

    @GetMapping
    public List<Transaction> getTransactions() {
        return List.of(
            new Transaction((long) 1, (long) 1, "test", new BigDecimal("500.00"), TransactionType.INCOME)
        );
    }
}
