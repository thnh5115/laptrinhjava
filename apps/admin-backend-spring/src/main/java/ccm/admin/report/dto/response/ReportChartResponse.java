package ccm.admin.report.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class ReportChartResponse {
    
    
    private Map<String, Long> transactionsByMonth;
    
    
    private Map<String, Double> revenueByMonth;
}
