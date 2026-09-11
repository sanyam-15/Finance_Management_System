package com.syfe.financemanager.repository;

import com.syfe.financemanager.model.Category;
import com.syfe.financemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for categories.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrUserIsNull(User user);
    Optional<Category> findByNameAndUser(String name, User user);
    Optional<Category> findByNameAndUserIsNull(String name);
}
