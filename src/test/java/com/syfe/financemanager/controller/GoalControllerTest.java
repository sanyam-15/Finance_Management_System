package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.GoalRequest;
import com.syfe.financemanager.dto.GoalResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.GoalService;
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
class GoalControllerTest {

    @Mock
    private GoalService goalService;

    @InjectMocks
    private GoalController goalController;

    private final User user = User.builder().id(1L).username("user@example.com").build();
    private final GoalResponse goal = GoalResponse.builder().id(1L).goalName("Emergency Fund").targetAmount(5000.0).build();

    @Test
    void createGoal_returnsCreated() {
        GoalRequest request = GoalRequest.builder().goalName("Emergency Fund").targetAmount(5000.0).targetDate("2030-01-01").build();
        when(goalService.createGoal(user, request)).thenReturn(goal);

        ResponseEntity<GoalResponse> response = goalController.createGoal(user, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getGoals_wrapsList() {
        when(goalService.getGoals(user)).thenReturn(List.of(goal));

        ResponseEntity<?> response = goalController.getGoals(user);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(1, ((List<?>) body.get("goals")).size());
    }

    @Test
    void getGoal_returnsOk() {
        when(goalService.getGoal(user, 1L)).thenReturn(goal);

        ResponseEntity<GoalResponse> response = goalController.getGoal(user, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Emergency Fund", response.getBody().getGoalName());
    }

    @Test
    void updateGoal_returnsOk() {
        GoalRequest request = GoalRequest.builder().targetAmount(6000.0).build();
        when(goalService.updateGoal(user, 1L, request)).thenReturn(goal);

        ResponseEntity<GoalResponse> response = goalController.updateGoal(user, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteGoal_returnsMessage() {
        ResponseEntity<?> response = goalController.deleteGoal(user, 1L);

        verify(goalService).deleteGoal(user, 1L);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Goal deleted successfully", body.get("message"));
    }
}
