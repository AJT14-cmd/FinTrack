package com.ajthapa.category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
@Tag(name = "Categories", description = "Manage the authenticated user's income and expense categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "List the authenticated user's categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping
    public List<CategoryResponse> getCategories(@AuthenticationPrincipal Jwt jwt) {
        return categoryService.getCategories(Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Get one of the authenticated user's categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Category does not exist or belongs to another user")
    })
    @GetMapping("{id}")
    public CategoryResponse getCategoriesById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return categoryService.getCategoriesById(id, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Create a category", description = "Ownership is assigned from the authenticated user; no owner ID is accepted.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest,
                                                           @AuthenticationPrincipal Jwt jwt) {
        CategoryResponse categoryResponse = categoryService.createCategory(createCategoryRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponse);
    }

    @Operation(summary = "Delete one of the authenticated user's categories")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Category does not exist or belongs to another user")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        categoryService.deleteCategory(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update one of the authenticated user's categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Category does not exist or belongs to another user")
    })
    @PutMapping("{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest,
                                                           @AuthenticationPrincipal Jwt jwt) {
        CategoryResponse categoryResponse = categoryService.updateCategory(id, updateCategoryRequest, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(categoryResponse);
    }
}
