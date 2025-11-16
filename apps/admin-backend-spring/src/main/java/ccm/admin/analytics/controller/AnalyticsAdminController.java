package ccm.admin.analytics.controller;

import ccm.admin.analytics.dto.response.DisputeRatioResponse;
import ccm.admin.analytics.dto.response.SystemKpiResponse;
import ccm.admin.analytics.dto.response.TransactionTrendResponse;
import ccm.admin.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
/** Analytics - REST Controller - Admin endpoints for Analytics management */

public class AnalyticsAdminController {

    private final AnalyticsService analyticsService;

    
    /** GET /api/admin/analytics/kpis - calculate KPIs */
    @GetMapping("/kpis")
    public SystemKpiResponse getKpis() {
        log.info("GET /api/admin/analytics/kpis - Fetching system KPIs");
        return analyticsService.getSystemKpis();
    }

    
    @GetMapping("/transactions")
    public TransactionTrendResponse getTransactionTrends(
            @RequestParam(defaultValue = "2025") int year
    ) {
        log.info("GET /api/admin/analytics/transactions - Fetching trends for year: {}", year);
        return analyticsService.getTransactionTrends(year);
    }

    
    /** GET /api/admin/analytics/disputes - perform operation */
    @GetMapping("/disputes")
    public DisputeRatioResponse getDisputeRatios() {
        log.info("GET /api/admin/analytics/disputes - Fetching dispute ratios");
        return analyticsService.getDisputeRatios();
    }
}
