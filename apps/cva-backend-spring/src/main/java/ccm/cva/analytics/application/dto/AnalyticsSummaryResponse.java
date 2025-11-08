package ccm.cva.analytics.application.dto;

import java.math.BigDecimal;

/**
 * Top-level CVA dashboard counters that back the summary tiles on the web console.
 */
public record AnalyticsSummaryResponse(
        long totalRequests,
        long pendingRequests,
        long approvedRequests,
        long rejectedRequests,
        double approvalRate,
        double rejectionRate,
        BigDecimal totalCreditsIssued
) {
}
