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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@example.com").build();
    }

    @Test
    void initializeDefaultCategories_createsMissingDefaults() {
        when(categoryRepository.findByNameAndUserIsNull(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        categoryService.initializeDefaultCategories();

        verify(categoryRepository, times(7)).save(any(Category.class));
    }

    @Test
    void getCategories_returnsMappedList() {
        Category salary = Category.builder().name("Salary").type(CategoryType.INCOME).isCustom(false).build();
        when(categoryRepository.findByUserOrUserIsNull(user)).thenReturn(List.of(salary));

        List<CategoryResponse> result = categoryService.getCategories(user);

        assertEquals(1, result.size());
        assertEquals("Salary", result.get(0).getName());
        assertFalse(result.get(0).isCustom());
    }

    @Test
    void createCategory_success() {
        CategoryRequest request = CategoryRequest.builder().name("Freelance").type("INCOME").build();
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = categoryService.createCategory(user, request);

        assertEquals("Freelance", response.getName());
        assertEquals("INCOME", response.getType());
        assertTrue(response.isCustom());
    }

    @Test
    void createCategory_duplicate_throwsConflict() {
        CategoryRequest request = CategoryRequest.builder().name("Food").type("EXPENSE").build();
        when(categoryRepository.findByNameAndUser("Food", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Food"))
                .thenReturn(Optional.of(Category.builder().name("Food").type(CategoryType.EXPENSE).build()));

        assertThrows(ConflictException.class, () -> categoryService.createCategory(user, request));
    }

    @Test
    void createCategory_invalidType_throwsBadRequest() {
        CategoryRequest request = CategoryRequest.builder().name("X").type("OTHER").build();
        assertThrows(BadRequestException.class, () -> categoryService.createCategory(user, request));
    }

    @Test
    void createCategory_blankName_throwsBadRequest() {
        CategoryRequest request = CategoryRequest.builder().name("  ").type("INCOME").build();
        assertThrows(BadRequestException.class, () -> categoryService.createCategory(user, request));
    }

    @Test
    void deleteCategory_success() {
        Category custom = Category.builder().id(10L).name("Side").type(CategoryType.INCOME).isCustom(true).user(user).build();
        when(categoryRepository.findByNameAndUser("Side", user)).thenReturn(Optional.of(custom));
        when(transactionRepository.existsByCategoryId(10L)).thenReturn(false);

        categoryService.deleteCategory(user, "Side");

        verify(categoryRepository).delete(custom);
    }

    @Test
    void deleteCategory_default_throwsForbidden() {
        when(categoryRepository.findByNameAndUser("Food", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Food"))
                .thenReturn(Optional.of(Category.builder().name("Food").build()));

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCategory(user, "Food"));
    }

    @Test
    void deleteCategory_inUse_throwsBadRequest() {
        Category custom = Category.builder().id(10L).name("Side").type(CategoryType.EXPENSE).user(user).build();
        when(categoryRepository.findByNameAndUser("Side", user)).thenReturn(Optional.of(custom));
        when(transactionRepository.existsByCategoryId(10L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory(user, "Side"));
    }

    @Test
    void deleteCategory_missing_throwsNotFound() {
        when(categoryRepository.findByNameAndUser("Missing", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(user, "Missing"));
    }
}
