package com.syfe.financemanager.repository;

import com.syfe.financemanager.model.Goal;
import com.syfe.financemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for savings goals.
 */
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUser(User user);
}
