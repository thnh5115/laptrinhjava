package ccm.admin.report.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class ReportSummaryResponse {
    
    
    private long totalUsers;
    
    
    private long totalTransactions;
    
    
    private double totalRevenue;
    
    
    private long approvedTransactions;
    
    
    private long rejectedTransactions;
    
    
    private long pendingTransactions;
}
