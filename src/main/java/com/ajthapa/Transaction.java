package com.ajthapa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private Long id;
    private Long accountId;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private final LocalDateTime transactionDateTime;

    public Transaction(Long id, Long accountId, String description, BigDecimal amount,
                       TransactionType type) {
        this.id = id;
        this.accountId = accountId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.transactionDateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) && Objects.equals(accountId, that.accountId) &&
                Objects.equals(description, that.description) && Objects.equals(amount, that.amount)
                && type == that.type && Objects.equals(transactionDateTime, that.transactionDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, description, amount, type, transactionDateTime);
    }
}