package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.GoalRequest;
import com.syfe.financemanager.dto.GoalResponse;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ForbiddenException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.model.Category;
import com.syfe.financemanager.model.CategoryType;
import com.syfe.financemanager.model.Goal;
import com.syfe.financemanager.model.Transaction;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.GoalRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GoalService goalService;

    private User user;
    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@example.com").build();
        salary = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).build();
        food = Category.builder().id(2L).name("Food").type(CategoryType.EXPENSE).build();
    }

    @Test
    void createGoal_successWithProgress() {
        GoalRequest request = GoalRequest.builder()
                .goalName("Emergency Fund")
                .targetAmount(5000.0)
                .targetDate("2030-01-01")
                .startDate("2024-01-01")
                .build();

        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any()))
                .thenReturn(List.of(
                        Transaction.builder().amount(BigDecimal.valueOf(3000)).type(CategoryType.INCOME).category(salary).user(user).build(),
                        Transaction.builder().amount(BigDecimal.valueOf(1000)).type(CategoryType.EXPENSE).category(food).user(user).build()
                ));

        GoalResponse response = goalService.createGoal(user, request);

        assertEquals(1L, response.getId());
        assertEquals(2000.0, response.getCurrentProgress());
        assertEquals(40.0, response.getProgressPercentage());
        assertEquals(3000.0, response.getRemainingAmount());
    }

    @Test
    void createGoal_startAfterTarget_throwsBadRequest() {
        GoalRequest request = GoalRequest.builder()
                .goalName("Bad")
                .targetAmount(1000.0)
                .targetDate("2026-01-01")
                .startDate("2027-01-01")
                .build();

        assertThrows(BadRequestException.class, () -> goalService.createGoal(user, request));
    }

    @Test
    void createGoal_pastTarget_throwsBadRequest() {
        GoalRequest request = GoalRequest.builder()
                .goalName("Bad")
                .targetAmount(1000.0)
                .targetDate("2020-01-01")
                .build();

        assertThrows(BadRequestException.class, () -> goalService.createGoal(user, request));
    }

    @Test
    void createGoal_negativeAmount_throwsBadRequest() {
        GoalRequest request = GoalRequest.builder()
                .goalName("Bad")
                .targetAmount(-5.0)
                .targetDate("2030-01-01")
                .build();

        assertThrows(BadRequestException.class, () -> goalService.createGoal(user, request));
    }

    @Test
    void getGoal_otherUser_throwsForbidden() {
        User other = User.builder().id(2L).build();
        Goal goal = Goal.builder()
                .id(1L)
                .goalName("G")
                .targetAmount(BigDecimal.valueOf(100))
                .targetDate(LocalDate.of(2030, 1, 1))
                .startDate(LocalDate.of(2024, 1, 1))
                .user(other)
                .build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class, () -> goalService.getGoal(user, 1L));
    }

    @Test
    void updateGoal_success() {
        Goal goal = Goal.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(BigDecimal.valueOf(5000))
                .targetDate(LocalDate.of(2030, 1, 1))
                .startDate(LocalDate.of(2024, 1, 1))
                .user(user)
                .build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any())).thenReturn(List.of());

        GoalResponse response = goalService.updateGoal(user, 1L, GoalRequest.builder()
                .targetAmount(6000.0)
                .targetDate("2031-02-01")
                .build());

        assertEquals(6000.0, response.getTargetAmount());
        assertEquals("2031-02-01", response.getTargetDate());
    }

    @Test
    void deleteGoal_missing_throwsNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> goalService.deleteGoal(user, 99L));
    }

    @Test
    void getGoals_returnsList() {
        Goal goal = Goal.builder()
                .id(1L)
                .goalName("G")
                .targetAmount(BigDecimal.valueOf(1000))
                .targetDate(LocalDate.of(2030, 1, 1))
                .startDate(LocalDate.of(2024, 1, 1))
                .user(user)
                .build();
        when(goalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any())).thenReturn(List.of());

        List<GoalResponse> goals = goalService.getGoals(user);
        assertEquals(1, goals.size());
        assertEquals(0.0, goals.get(0).getCurrentProgress());
    }

    @Test
    void createGoal_defaultsStartDateToToday() {
        GoalRequest request = GoalRequest.builder()
                .goalName("No Start")
                .targetAmount(1000.0)
                .targetDate("2030-01-01")
                .build();
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(5L);
            return g;
        });
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any())).thenReturn(List.of());

        GoalResponse response = goalService.createGoal(user, request);
        assertEquals(LocalDate.now().toString(), response.getStartDate());
    }

    @Test
    void createGoal_blankName_throwsBadRequest() {
        GoalRequest request = GoalRequest.builder()
                .goalName(" ")
                .targetAmount(1000.0)
                .targetDate("2030-01-01")
                .build();
        assertThrows(BadRequestException.class, () -> goalService.createGoal(user, request));
    }

    @Test
    void updateGoal_pastTargetDate_throwsBadRequest() {
        Goal goal = Goal.builder()
                .id(1L)
                .goalName("G")
                .targetAmount(BigDecimal.valueOf(5000))
                .targetDate(LocalDate.of(2030, 1, 1))
                .startDate(LocalDate.of(2024, 1, 1))
                .user(user)
                .build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(BadRequestException.class, () -> goalService.updateGoal(user, 1L,
                GoalRequest.builder().targetDate("2020-01-01").build()));
    }

    @Test
    void updateGoal_nonPositiveAmount_throwsBadRequest() {
        Goal goal = Goal.builder()
                .id(1L)
                .goalName("G")
                .targetAmount(BigDecimal.valueOf(5000))
                .targetDate(LocalDate.of(2030, 1, 1))
                .startDate(LocalDate.of(2024, 1, 1))
                .user(user)
                .build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(BadRequestException.class, () -> goalService.updateGoal(user, 1L,
                GoalRequest.builder().targetAmount(0.0).build()));
    }

    @Test
    void progressIsCappedAtOneHundredPercent() {
        Goal goal = Goal.builder()
                .id(1L)
                .goalName("G")
                .targetAmount(BigDecimal.valueOf(100))
                .targetDate(LocalDate.of(2030, 1, 1))
                .startDate(LocalDate.of(2024, 1, 1))
                .user(user)
                .build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any()))
                .thenReturn(List.of(Transaction.builder()
                        .amount(BigDecimal.valueOf(500)).type(CategoryType.INCOME).category(salary).user(user).build()));

        GoalResponse response = goalService.getGoal(user, 1L);
        assertEquals(100.0, response.getProgressPercentage());
        assertEquals(0.0, response.getRemainingAmount());
    }

    @Test
    void deleteGoal_success() {
        Goal goal = Goal.builder()
                .id(1L)
                .goalName("G")
                .targetAmount(BigDecimal.valueOf(100))
                .targetDate(LocalDate.of(2030, 1, 1))
                .startDate(LocalDate.of(2024, 1, 1))
                .user(user)
                .build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        goalService.deleteGoal(user, 1L);
        verify(goalRepository).delete(goal);
    }
}
