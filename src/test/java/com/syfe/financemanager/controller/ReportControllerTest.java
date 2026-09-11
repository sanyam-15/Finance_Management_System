package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.MonthlyReportResponse;
import com.syfe.financemanager.dto.YearlyReportResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private final User user = User.builder().id(1L).username("user@example.com").build();

    @Test
    void getMonthlyReport_returnsOk() {
        MonthlyReportResponse report = MonthlyReportResponse.builder()
                .month(1).year(2024).totalIncome(Map.of("Salary", 3000.0)).netSavings(3000.0).build();
        when(reportService.getMonthlyReport(user, 2024, 1)).thenReturn(report);

        ResponseEntity<MonthlyReportResponse> response = reportController.getMonthlyReport(user, 2024, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3000.0, response.getBody().getNetSavings());
    }

    @Test
    void getYearlyReport_returnsOk() {
        YearlyReportResponse report = YearlyReportResponse.builder().year(2024).netSavings(100.0).build();
        when(reportService.getYearlyReport(user, 2024)).thenReturn(report);

        ResponseEntity<YearlyReportResponse> response = reportController.getYearlyReport(user, 2024);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2024, response.getBody().getYear());
    }
}
