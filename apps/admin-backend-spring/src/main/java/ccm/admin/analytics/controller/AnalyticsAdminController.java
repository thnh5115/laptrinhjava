package ccm.admin.analytics.controller;

import ccm.admin.analytics.dto.response.DisputeRatioResponse;
import ccm.admin.analytics.dto.response.SystemKpiResponse;
import ccm.admin.analytics.dto.response.TransactionTrendResponse;
import ccm.admin.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Analytics Dashboard (Admin Only)
 * Provides endpoints for system KPIs, trends, and statistics
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsAdminController {

    private final AnalyticsService analyticsService;

    /**
     * Get system-wide KPI metrics
     * Includes: total users, transactions, disputes, revenue, and dispute rate
     *
     * @return System KPI metrics for dashboard widgets
     */
    @GetMapping("/kpis")
    public SystemKpiResponse getKpis() {
        log.info("GET /api/admin/analytics/kpis - Fetching system KPIs");
        return analyticsService.getSystemKpis();
    }

    /**
     * Get transaction trends for a specific year
     * Provides monthly breakdown for line charts
     *
     * @param year Year to analyze (e.g., 2025)
     * @return Monthly transaction counts and revenue data
     */
    @GetMapping("/transactions")
    public TransactionTrendResponse getTransactionTrends(
            @RequestParam(defaultValue = "2025") int year
    ) {
        log.info("GET /api/admin/analytics/transactions - Fetching trends for year: {}", year);
        return analyticsService.getTransactionTrends(year);
    }

    /**
     * Get dispute ratio statistics
     * Provides breakdown for pie charts
     *
     * @return Dispute counts by status (OPEN, RESOLVED, REJECTED)
     */
    @GetMapping("/disputes")
    public DisputeRatioResponse getDisputeRatios() {
        log.info("GET /api/admin/analytics/disputes - Fetching dispute ratios");
        return analyticsService.getDisputeRatios();
    }
}
