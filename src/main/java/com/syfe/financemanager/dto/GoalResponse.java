package com.syfe.financemanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoalResponse {
    private Long id;
    private String goalName;
    private Double targetAmount;
    private String targetDate;
    private String startDate;
    private Double currentProgress;
    private Double progressPercentage;
    private Double remainingAmount;
}
