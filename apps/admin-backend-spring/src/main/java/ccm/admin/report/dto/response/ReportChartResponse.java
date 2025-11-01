package ccm.admin.report.dto.response;

import lombok.*;

import java.util.Map;

/**
 * Chart response for monthly reports
 * Contains time-series data for transactions and revenue
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportChartResponse {
    
    /**
     * Number of transactions per month
     * Key format: "YYYY-MM" (e.g., "2025-01")
     * Value: transaction count
     */
    private Map<String, Long> transactionsByMonth;
    
    /**
     * Total revenue per month (approved transactions only)
     * Key format: "YYYY-MM" (e.g., "2025-01")
     * Value: total revenue amount
     */
    private Map<String, Double> revenueByMonth;
}
