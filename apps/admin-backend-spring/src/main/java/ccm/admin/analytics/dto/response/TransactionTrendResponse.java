package ccm.admin.analytics.dto.response;

import lombok.*;

import java.util.Map;

/**
 * Response DTO for Transaction Trend Analysis
 * Provides monthly time-series data for charts
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTrendResponse {
    
    /**
     * Number of transactions per month
     * Key format: "YYYY-MM" (e.g., "2025-01")
     * Value: Transaction count for that month
     */
    private Map<String, Long> monthlyTransactions;
    
    /**
     * Revenue per month (from approved transactions only)
     * Key format: "YYYY-MM" (e.g., "2025-01")
     * Value: Total revenue for that month
     */
    private Map<String, Double> monthlyRevenue;
}
