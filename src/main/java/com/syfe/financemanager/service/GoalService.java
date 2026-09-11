package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.GoalRequest;
import com.syfe.financemanager.dto.GoalResponse;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ForbiddenException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.model.CategoryType;
import com.syfe.financemanager.model.Goal;
import com.syfe.financemanager.model.Transaction;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.GoalRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for savings goals and progress tracking.
 */
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates a savings goal. Progress is computed from net savings since the start date.
     *
     * @param user    the authenticated user
     * @param request goal payload
     * @return created goal with progress metrics
     */
    @Transactional
    public GoalResponse createGoal(User user, GoalRequest request) {
        validateGoalRequest(request);

        LocalDate targetDate = LocalDate.parse(request.getTargetDate());
        if (!targetDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        LocalDate startDate = request.getStartDate() != null ? LocalDate.parse(request.getStartDate()) : LocalDate.now();
        if (startDate.isAfter(targetDate)) {
            throw new BadRequestException("Start date cannot be after target date");
        }

        Goal goal = Goal.builder()
                .goalName(request.getGoalName())
                .targetAmount(BigDecimal.valueOf(request.getTargetAmount()))
                .targetDate(targetDate)
                .startDate(startDate)
                .user(user)
                .build();

        Goal saved = goalRepository.save(goal);
        return mapToResponse(saved, calculateProgress(saved));
    }

    /**
     * Lists all savings goals for the user with live progress.
     */
    public List<GoalResponse> getGoals(User user) {
        return goalRepository.findByUser(user).stream()
                .map(goal -> mapToResponse(goal, calculateProgress(goal)))
                .collect(Collectors.toList());
    }

    /**
     * Returns a single goal owned by the user.
     */
    public GoalResponse getGoal(User user, Long id) {
        Goal goal = getGoalByIdAndUser(id, user);
        return mapToResponse(goal, calculateProgress(goal));
    }

    /**
     * Updates target amount and/or target date for an owned goal.
     */
    @Transactional
    public GoalResponse updateGoal(User user, Long id, GoalRequest request) {
        Goal goal = getGoalByIdAndUser(id, user);

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount() <= 0) throw new BadRequestException("Amount must be positive");
            goal.setTargetAmount(BigDecimal.valueOf(request.getTargetAmount()));
        }
        if (request.getTargetDate() != null) {
            LocalDate targetDate = LocalDate.parse(request.getTargetDate());
            if (!targetDate.isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            if (goal.getStartDate().isAfter(targetDate)) {
                throw new BadRequestException("Start date cannot be after target date");
            }
            goal.setTargetDate(targetDate);
        }

        Goal saved = goalRepository.save(goal);
        return mapToResponse(saved, calculateProgress(saved));
    }

    /**
     * Deletes an owned savings goal.
     */
    @Transactional
    public void deleteGoal(User user, Long id) {
        Goal goal = getGoalByIdAndUser(id, user);
        goalRepository.delete(goal);
    }

    private Goal getGoalByIdAndUser(Long id, User user) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot access this goal");
        }
        return goal;
    }

    private BigDecimal calculateProgress(Goal goal) {
        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(
                goal.getUser(), goal.getStartDate(), LocalDate.now());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getType() == CategoryType.EXPENSE) {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        BigDecimal net = totalIncome.subtract(totalExpense);
        return net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO;
    }

    private void validateGoalRequest(GoalRequest request) {
        if (request.getGoalName() == null || request.getGoalName().isBlank()) {
            throw new BadRequestException("Goal name is required");
        }
        if (request.getTargetAmount() == null || request.getTargetAmount() <= 0) {
            throw new BadRequestException("Target amount must be positive");
        }
        if (request.getTargetDate() == null) {
            throw new BadRequestException("Target date is required");
        }
    }

    private GoalResponse mapToResponse(Goal goal, BigDecimal progress) {
        BigDecimal percentage = progress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            percentage = BigDecimal.valueOf(100);
        }

        BigDecimal remaining = goal.getTargetAmount().subtract(progress);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount().setScale(2, RoundingMode.HALF_UP).doubleValue())
                .targetDate(goal.getTargetDate().toString())
                .startDate(goal.getStartDate().toString())
                .currentProgress(progress.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .progressPercentage(percentage.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .remainingAmount(remaining.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build();
    }
}
