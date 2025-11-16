package ccm.admin.analytics.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class DisputeRatioResponse {
    
    
    private long openCount;
    
    
    private long resolvedCount;
    
    
    private long rejectedCount;
    
    
    private long total;
}
