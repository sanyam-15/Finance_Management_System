package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.MonthlyReportResponse;
import com.syfe.financemanager.dto.YearlyReportResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for monthly and yearly financial reports.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Returns a monthly income/expense report.
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@AuthenticationPrincipal User user,
                                                                  @PathVariable int year,
                                                                  @PathVariable int month) {
        MonthlyReportResponse response = reportService.getMonthlyReport(user, year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a yearly income/expense report.
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@AuthenticationPrincipal User user,
                                                                @PathVariable int year) {
        YearlyReportResponse response = reportService.getYearlyReport(user, year);
        return ResponseEntity.ok(response);
    }
}
