package com.syfe.financemanager.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class MonthlyReportResponse {
    private Integer month;
    private Integer year;
    private Map<String, Double> totalIncome;
    private Map<String, Double> totalExpenses;
    private Double netSavings;
}
