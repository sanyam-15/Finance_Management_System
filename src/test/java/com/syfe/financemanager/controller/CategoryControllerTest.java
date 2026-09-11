package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.CategoryRequest;
import com.syfe.financemanager.dto.CategoryResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private final User user = User.builder().id(1L).username("user@example.com").build();

    @Test
    void getCategories_wrapsList() {
        when(categoryService.getCategories(user))
                .thenReturn(List.of(CategoryResponse.builder().name("Salary").type("INCOME").isCustom(false).build()));

        ResponseEntity<?> response = categoryController.getCategories(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(1, ((List<?>) body.get("categories")).size());
    }

    @Test
    void createCategory_returnsCreated() {
        CategoryRequest request = CategoryRequest.builder().name("Freelance").type("INCOME").build();
        CategoryResponse created = CategoryResponse.builder().name("Freelance").type("INCOME").isCustom(true).build();
        when(categoryService.createCategory(user, request)).thenReturn(created);

        ResponseEntity<CategoryResponse> response = categoryController.createCategory(user, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Freelance", response.getBody().getName());
    }

    @Test
    void deleteCategory_returnsMessage() {
        ResponseEntity<?> response = categoryController.deleteCategory(user, "Freelance");

        verify(categoryService).deleteCategory(user, "Freelance");
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Category deleted successfully", body.get("message"));
    }
}
