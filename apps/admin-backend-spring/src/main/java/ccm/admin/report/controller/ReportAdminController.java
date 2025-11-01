package ccm.admin.report.controller;

import ccm.admin.report.dto.response.ReportChartResponse;
import ccm.admin.report.dto.response.ReportSummaryResponse;
import ccm.admin.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Admin Report Management
 * Provides endpoints for dashboard statistics and chart data
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportAdminController {

    private final ReportService reportService;

    /**
     * GET /api/admin/reports/summary
     * Get summary statistics for dashboard
     * 
     * Returns aggregated data:
     * - Total users
     * - Total transactions (all statuses)
     * - Total revenue (approved transactions only)
     * - Approved/Rejected/Pending transaction counts
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportSummaryResponse> getSummary() {
        ReportSummaryResponse summary = reportService.getSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/admin/reports/monthly?year=2025
     * Get monthly report for a specific year
     * 
     * Returns time-series data:
     * - Transaction count per month (all statuses)
     * - Revenue per month (approved transactions only)
     * 
     * @param year Year to generate report for (e.g., 2025)
     * @return Monthly chart data with transactions and revenue by month
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportChartResponse> getMonthlyReport(
            @RequestParam(defaultValue = "2025") int year) {
        
        ReportChartResponse monthlyReport = reportService.getMonthlyReport(year);
        return ResponseEntity.ok(monthlyReport);
    }
}
