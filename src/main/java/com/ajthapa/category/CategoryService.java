package com.ajthapa.category;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream().map(this::mapResponse).toList();
    }

    public CategoryResponse getCategoriesById(Long id) {
        return categoryRepository.findById(id).map(this::mapResponse)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));
    }

    public CategoryResponse createCategory(CreateCategoryRequest createCategoryRequest) {
        Category category = new Category(
                null,
                createCategoryRequest.name(),
                createCategoryRequest.type()
        );

        categoryRepository.save(category);

        return mapResponse(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        categoryRepository.delete(category);
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest updateCategoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " not found"));

        category.setName(updateCategoryRequest.name());
        category.setType(updateCategoryRequest.type());

        categoryRepository.save(category);

        return mapResponse(category);
    }

    private CategoryResponse mapResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType()
        );
    }
}
