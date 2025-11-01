package ccm.admin.report.service;

import ccm.admin.report.dto.response.ReportChartResponse;
import ccm.admin.report.dto.response.ReportSummaryResponse;

/**
 * Service interface for report generation and statistics
 */
public interface ReportService {
    
    /**
     * Get summary statistics for dashboard
     * Includes total users, transactions, revenue, and status breakdown
     * 
     * @return Summary statistics
     */
    ReportSummaryResponse getSummary();
    
    /**
     * Get monthly report for a specific year
     * Includes transactions count and revenue per month
     * 
     * @param year Year to generate report for (e.g., 2025)
     * @return Monthly chart data
     */
    ReportChartResponse getMonthlyReport(int year);
}
