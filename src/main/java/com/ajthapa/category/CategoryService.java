package com.ajthapa.category;

import com.ajthapa.user.AppUser;
import com.ajthapa.user.AppUserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;

    public CategoryService(CategoryRepository categoryRepository, AppUserRepository appUserRepository) {
        this.categoryRepository = categoryRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<CategoryResponse> getCategories(Long userId) {
        return categoryRepository.findAllByAppUserId(userId)
                .stream().map(this::mapResponse).toList();
    }

    public CategoryResponse getCategoriesById(Long id, Long userId) {
        return categoryRepository.findByIdAndAppUserId(id, userId).map(this::mapResponse)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));
    }

    public CategoryResponse createCategory(CreateCategoryRequest createCategoryRequest, Long userId) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User " + userId + " not found"));

        Category category = new Category(
                null,
                createCategoryRequest.name(),
                createCategoryRequest.type(),
                appUser
        );

        Category savedCategory = categoryRepository.save(category);

        return mapResponse(savedCategory);
    }

    public void deleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findByIdAndAppUserId(id, userId)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        categoryRepository.delete(category);
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest updateCategoryRequest, Long userId) {
        Category category = categoryRepository.findByIdAndAppUserId(id, userId)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        category.setName(updateCategoryRequest.name());
        category.setType(updateCategoryRequest.type());

        Category savedCategory = categoryRepository.save(category);

        return mapResponse(savedCategory);
    }

    private CategoryResponse mapResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType()
        );
    }
}
