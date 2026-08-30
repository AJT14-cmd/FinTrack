package com.ajthapa.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import com.ajthapa.category.Category;

@Entity
@Table(name="financial_transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    private Long accountId;
    private String description;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private LocalDateTime transactionDateTime;

    public Transaction(Long id, Category category, Long accountId, String description, BigDecimal amount,
                       TransactionType type) {
        this.id = id;
        this.category = category;
        this.accountId = accountId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.transactionDateTime = LocalDateTime.now();
    }

    protected Transaction() {

    }

    public Long getId() {
        return id;
    }

    public Category getCategory() { return category; }

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

    public void setCategory(Category category) { this.category = category; }

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
                && type == that.type && Objects.equals(transactionDateTime, that.transactionDateTime) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, accountId, description, amount, type, transactionDateTime);
    }
}