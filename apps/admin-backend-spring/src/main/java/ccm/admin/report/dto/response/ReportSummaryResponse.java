package ccm.admin.report.dto.response;

import lombok.*;

/**
 * Summary response for dashboard overview
 * Contains aggregated statistics for users, transactions, and revenue
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryResponse {
    
    /**
     * Total number of users in the system
     */
    private long totalUsers;
    
    /**
     * Total number of transactions (all statuses)
     */
    private long totalTransactions;
    
    /**
     * Total revenue from approved transactions
     */
    private double totalRevenue;
    
    /**
     * Number of approved transactions
     */
    private long approvedTransactions;
    
    /**
     * Number of rejected transactions
     */
    private long rejectedTransactions;
    
    /**
     * Number of pending transactions
     */
    private long pendingTransactions;
}
