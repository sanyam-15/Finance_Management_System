package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.CategoryRequest;
import com.syfe.financemanager.dto.CategoryResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for listing, creating, and deleting categories.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Lists default and custom categories available to the user.
     */
    @GetMapping
    public ResponseEntity<?> getCategories(@AuthenticationPrincipal User user) {
        List<CategoryResponse> categories = categoryService.getCategories(user);
        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a custom category for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Deletes a custom category by name.
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteCategory(@AuthenticationPrincipal User user,
                                            @PathVariable String name) {
        categoryService.deleteCategory(user, name);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        return ResponseEntity.ok(response);
    }
}
