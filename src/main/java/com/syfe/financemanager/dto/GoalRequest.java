package com.syfe.financemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating or updating a savings goal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {
    private String goalName;
    private Double targetAmount;
    private String targetDate;
    private String startDate;
}
