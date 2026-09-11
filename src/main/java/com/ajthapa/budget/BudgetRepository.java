package com.ajthapa.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByAppUserId(Long appUserId);
    List<Budget> findByAppUserIdAndMonth(Long appUserId, String month);
    Optional<Budget> findByIdAndAppUserId(Long budgetId, Long userId);
}
