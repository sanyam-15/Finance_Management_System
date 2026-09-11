package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.MonthlyReportResponse;
import com.syfe.financemanager.dto.YearlyReportResponse;
import com.syfe.financemanager.model.Category;
import com.syfe.financemanager.model.CategoryType;
import com.syfe.financemanager.model.Transaction;
import com.syfe.financemanager.model.User;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        salary = Category.builder().name("Salary").type(CategoryType.INCOME).build();
        food = Category.builder().name("Food").type(CategoryType.EXPENSE).build();
    }

    @Test
    void getMonthlyReport_aggregatesByCategory() {
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any()))
                .thenReturn(List.of(
                        Transaction.builder().amount(BigDecimal.valueOf(3000)).type(CategoryType.INCOME).category(salary).date(LocalDate.of(2024, 1, 10)).build(),
                        Transaction.builder().amount(BigDecimal.valueOf(500)).type(CategoryType.INCOME).category(salary).date(LocalDate.of(2024, 1, 20)).build(),
                        Transaction.builder().amount(BigDecimal.valueOf(400)).type(CategoryType.EXPENSE).category(food).date(LocalDate.of(2024, 1, 12)).build()
                ));

        MonthlyReportResponse report = reportService.getMonthlyReport(user, 2024, 1);

        assertEquals(1, report.getMonth());
        assertEquals(2024, report.getYear());
        assertEquals(3500.0, report.getTotalIncome().get("Salary"));
        assertEquals(400.0, report.getTotalExpenses().get("Food"));
        assertEquals(3100.0, report.getNetSavings());
    }

    @Test
    void getYearlyReport_aggregatesByCategory() {
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any()))
                .thenReturn(List.of(
                        Transaction.builder().amount(BigDecimal.valueOf(1000)).type(CategoryType.INCOME).category(salary).build(),
                        Transaction.builder().amount(BigDecimal.valueOf(200)).type(CategoryType.EXPENSE).category(food).build()
                ));

        YearlyReportResponse report = reportService.getYearlyReport(user, 2024);

        assertEquals(2024, report.getYear());
        assertEquals(1000.0, report.getTotalIncome().get("Salary"));
        assertEquals(200.0, report.getTotalExpenses().get("Food"));
        assertEquals(800.0, report.getNetSavings());
    }

    @Test
    void getMonthlyReport_invalidMonth_throws() {
        assertThrows(Exception.class, () -> reportService.getMonthlyReport(user, 2024, 13));
    }
}
