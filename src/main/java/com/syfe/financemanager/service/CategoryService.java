package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.CategoryRequest;
import com.syfe.financemanager.dto.CategoryResponse;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.exception.ForbiddenException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.model.Category;
import com.syfe.financemanager.model.CategoryType;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.CategoryRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for default and custom category management.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Seeds immutable default categories if they do not already exist.
     */
    @PostConstruct
    public void initializeDefaultCategories() {
        createDefaultCategoryIfNotFound("Salary", CategoryType.INCOME);
        createDefaultCategoryIfNotFound("Food", CategoryType.EXPENSE);
        createDefaultCategoryIfNotFound("Rent", CategoryType.EXPENSE);
        createDefaultCategoryIfNotFound("Transportation", CategoryType.EXPENSE);
        createDefaultCategoryIfNotFound("Entertainment", CategoryType.EXPENSE);
        createDefaultCategoryIfNotFound("Healthcare", CategoryType.EXPENSE);
        createDefaultCategoryIfNotFound("Utilities", CategoryType.EXPENSE);
    }

    private void createDefaultCategoryIfNotFound(String name, CategoryType type) {
        if (categoryRepository.findByNameAndUserIsNull(name).isEmpty()) {
            Category cat = Category.builder()
                    .name(name)
                    .type(type)
                    .isCustom(false)
                    .user(null)
                    .build();
            categoryRepository.save(cat);
        }
    }

    /**
     * Returns default categories plus the authenticated user's custom categories.
     */
    public List<CategoryResponse> getCategories(User user) {
        return categoryRepository.findByUserOrUserIsNull(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a custom category unique to the user.
     */
    @Transactional
    public CategoryResponse createCategory(User user, CategoryRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }
        if (request.getType() == null || (!request.getType().equals("INCOME") && !request.getType().equals("EXPENSE"))) {
            throw new BadRequestException("Invalid category type");
        }

        Optional<Category> existing = categoryRepository.findByNameAndUser(request.getName(), user);
        Optional<Category> existingDefault = categoryRepository.findByNameAndUserIsNull(request.getName());
        if (existing.isPresent() || existingDefault.isPresent()) {
            throw new ConflictException("Category already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(CategoryType.valueOf(request.getType()))
                .isCustom(true)
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    /**
     * Deletes a custom category that is not referenced by transactions.
     * Default categories cannot be deleted.
     */
    @Transactional
    public void deleteCategory(User user, String name) {
        Category category = categoryRepository.findByNameAndUser(name, user)
                .orElseThrow(() -> {
                    if (categoryRepository.findByNameAndUserIsNull(name).isPresent()) {
                        return new ForbiddenException("Cannot delete default category");
                    }
                    return new ResourceNotFoundException("Category not found");
                });

        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new BadRequestException("Category is in use");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType().name())
                .isCustom(category.isCustom())
                .build();
    }
}
