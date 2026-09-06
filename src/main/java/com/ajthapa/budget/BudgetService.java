package com.ajthapa.budget;

import com.ajthapa.category.Category;
import com.ajthapa.category.CategoryRepository;
import com.ajthapa.user.AppUser;
import com.ajthapa.user.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;

    public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository,
                         AppUserRepository appUserRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.appUserRepository = appUserRepository;
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
        AppUser appUser = appUserRepository.findById(createBudgetRequest.appUserId())
                .orElseThrow(() -> new IllegalStateException("User " + createBudgetRequest.appUserId() + " not found"));

        Budget budget = new Budget(
                null,
                category,
                appUser,
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
        AppUser appUser = appUserRepository.findById(updateBudgetRequest.appUserId())
                .orElseThrow(() -> new IllegalStateException("User " + updateBudgetRequest.appUserId() + " not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Budget " + id + " not found"));

        budget.setCategory(category);
        budget.setAppUser(appUser);
        budget.setMonth(updateBudgetRequest.month());
        budget.setLimitAmount(updateBudgetRequest.limitAmount());

        Budget savedBudget = budgetRepository.save(budget);

        return mapResponse(savedBudget);
    }

    public List<BudgetResponse> findByAppUserId(Long appUserId) {
        if (!appUserRepository.existsById(appUserId)) {
            throw new IllegalStateException("User " + appUserId + " not found");
        }

        return budgetRepository.findByAppUserId(appUserId).stream().map(this::mapResponse).toList();
    }

    private BudgetResponse mapResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getAppUser().getId(),
                budget.getAppUser().getName(),
                budget.getMonth(),
                budget.getLimitAmount()
        );
    }
}
