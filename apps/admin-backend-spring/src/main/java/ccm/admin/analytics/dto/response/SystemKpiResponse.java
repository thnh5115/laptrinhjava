package ccm.admin.analytics.dto.response;

import lombok.*;

/**
 * Response DTO for System KPI metrics
 * Provides overview statistics for admin dashboard
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemKpiResponse {
    
    /**
     * Total number of users in the system
     */
    private long totalUsers;
    
    /**
     * Total number of transactions
     */
    private long totalTransactions;
    
    /**
     * Total number of disputes raised
     */
    private long totalDisputes;
    
    /**
     * Total revenue from approved transactions
     */
    private double totalRevenue;
    
    /**
     * Dispute rate - percentage of transactions that resulted in disputes
     * Formula: (totalDisputes / totalTransactions) * 100
     */
    private double disputeRate;
}
