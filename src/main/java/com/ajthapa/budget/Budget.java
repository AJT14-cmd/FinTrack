package com.ajthapa.budget;

import com.ajthapa.category.Category;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    private String month;
    private BigDecimal limitAmount;

    public Budget(Long id, Category category, String month, BigDecimal limitAmount) {
        this.id = id;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    protected Budget() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }
}
