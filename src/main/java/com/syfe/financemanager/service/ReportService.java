package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.MonthlyReportResponse;
import com.syfe.financemanager.dto.YearlyReportResponse;
import com.syfe.financemanager.model.CategoryType;
import com.syfe.financemanager.model.Transaction;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for monthly and yearly financial reports.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    /**
     * Builds an income/expense breakdown and net savings for a given month.
     */
    public MonthlyReportResponse getMonthlyReport(User user, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, Double> totalIncome = new HashMap<>();
        Map<String, Double> totalExpenses = new HashMap<>();
        BigDecimal netSavings = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            if (t.getType() == CategoryType.INCOME) {
                totalIncome.put(catName, totalIncome.getOrDefault(catName, 0.0) + t.getAmount().doubleValue());
                netSavings = netSavings.add(t.getAmount());
            } else {
                totalExpenses.put(catName, totalExpenses.getOrDefault(catName, 0.0) + t.getAmount().doubleValue());
                netSavings = netSavings.subtract(t.getAmount());
            }
        }

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings.doubleValue())
                .build();
    }

    /**
     * Builds an income/expense breakdown and net savings for a given year.
     */
    public YearlyReportResponse getYearlyReport(User user, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, Double> totalIncome = new HashMap<>();
        Map<String, Double> totalExpenses = new HashMap<>();
        BigDecimal netSavings = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            if (t.getType() == CategoryType.INCOME) {
                totalIncome.put(catName, totalIncome.getOrDefault(catName, 0.0) + t.getAmount().doubleValue());
                netSavings = netSavings.add(t.getAmount());
            } else {
                totalExpenses.put(catName, totalExpenses.getOrDefault(catName, 0.0) + t.getAmount().doubleValue());
                netSavings = netSavings.subtract(t.getAmount());
            }
        }

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings.doubleValue())
                .build();
    }
}
