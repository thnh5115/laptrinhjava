package ccm.admin.analytics.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class SystemKpiResponse {
    
    
    private long totalUsers;
    
    
    private long totalTransactions;
    
    
    private long totalDisputes;
    
    
    private double totalRevenue;
    
    
    private double disputeRate;
}
