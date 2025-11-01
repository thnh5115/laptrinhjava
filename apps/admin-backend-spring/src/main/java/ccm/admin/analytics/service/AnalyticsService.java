package ccm.admin.analytics.service;

import ccm.admin.analytics.dto.response.DisputeRatioResponse;
import ccm.admin.analytics.dto.response.SystemKpiResponse;
import ccm.admin.analytics.dto.response.TransactionTrendResponse;

/**
 * Service interface for Analytics operations
 * Provides methods for dashboard KPIs, trends, and statistics
 */
public interface AnalyticsService {

    /**
     * Get system-wide KPI metrics
     * Includes: total users, transactions, disputes, revenue, and dispute rate
     *
     * @return System KPI metrics
     */
    SystemKpiResponse getSystemKpis();

    /**
     * Get transaction trends for a specific year
     * Provides monthly breakdown of transaction counts and revenue
     *
     * @param year Year to analyze (e.g., 2025)
     * @return Monthly transaction and revenue data
     */
    TransactionTrendResponse getTransactionTrends(int year);

    /**
     * Get dispute ratio statistics
     * Provides breakdown of disputes by status (OPEN, RESOLVED, REJECTED)
     *
     * @return Dispute ratio statistics
     */
    DisputeRatioResponse getDisputeRatios();
}
