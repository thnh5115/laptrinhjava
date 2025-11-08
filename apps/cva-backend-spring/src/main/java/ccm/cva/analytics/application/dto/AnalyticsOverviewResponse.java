package ccm.cva.analytics.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsOverviewResponse(
        long totalRequests,
        long pendingRequests,
        long approvedRequests,
        long rejectedRequests,
        double approvalRate,
        double rejectionRate,
        BigDecimal totalCreditsIssued,
        BigDecimal creditsIssuedInWindow,
        long requestsInWindow,
        List<DailyRequestMetric> recentTrend
) {
}
