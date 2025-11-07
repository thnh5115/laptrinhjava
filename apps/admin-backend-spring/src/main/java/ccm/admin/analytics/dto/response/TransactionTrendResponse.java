package ccm.admin.analytics.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class TransactionTrendResponse {
    
    
    private Map<String, Long> monthlyTransactions;
    
    
    private Map<String, Double> monthlyRevenue;
}
