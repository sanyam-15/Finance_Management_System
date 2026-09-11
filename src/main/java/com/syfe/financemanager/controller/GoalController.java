package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.GoalRequest;
import com.syfe.financemanager.dto.GoalResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.GoalService;
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
 * REST endpoints for savings goal CRUD operations.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * Creates a savings goal for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Lists all savings goals for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<?> getGoals(@AuthenticationPrincipal User user) {
        List<GoalResponse> goals = goalService.getGoals(user);
        Map<String, Object> response = new HashMap<>();
        response.put("goals", goals);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single savings goal by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@AuthenticationPrincipal User user,
                                                @PathVariable Long id) {
        GoalResponse response = goalService.getGoal(user, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates target amount and/or target date of a savings goal.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@AuthenticationPrincipal User user,
                                                   @PathVariable Long id,
                                                   @RequestBody GoalRequest request) {
        GoalResponse response = goalService.updateGoal(user, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@AuthenticationPrincipal User user,
                                        @PathVariable Long id) {
        goalService.deleteGoal(user, id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Goal deleted successfully");
        return ResponseEntity.ok(response);
    }
}
