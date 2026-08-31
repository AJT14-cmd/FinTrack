package com.ajthapa.budget;

import com.ajthapa.category.Category;
import com.ajthapa.category.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<BudgetResponse> getAllBudgets() {
        return budgetRepository.findAll().stream().map(this::mapResponse).toList();
    }

    public BudgetResponse getBudgetById(Long id) {
        return budgetRepository.findById(id).map(this::mapResponse).orElseThrow(() ->
                new IllegalStateException("Budget " + id + " not found"));
    }

    public BudgetResponse createBudget(CreateBudgetRequest createBudgetRequest) {
        Category category = categoryRepository.findById(createBudgetRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + createBudgetRequest.categoryId() + " not found"));

        Budget budget = new Budget(
                null,
                category,
                createBudgetRequest.month(),
                createBudgetRequest.limitAmount()
        );

        Budget savedBudget = budgetRepository.save(budget);

        return mapResponse(savedBudget);
    }

    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Budget " + id + " not found"));

        budgetRepository.delete(budget);

    }

    public BudgetResponse updateBudget(Long id, UpdateBudgetRequest updateBudgetRequest) {
        Category category = categoryRepository.findById(updateBudgetRequest.categoryId())
                .orElseThrow(() -> new IllegalStateException("Category " + updateBudgetRequest.categoryId() + " not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Budget " + id + " not found"));

        budget.setCategory(category);
        budget.setMonth(updateBudgetRequest.month());
        budget.setLimitAmount(updateBudgetRequest.limitAmount());

        Budget savedBudget = budgetRepository.save(budget);

        return mapResponse(savedBudget);
    }

    private BudgetResponse mapResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getMonth(),
                budget.getLimitAmount()
        );
    }
}
