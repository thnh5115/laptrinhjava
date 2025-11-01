package ccm.admin.analytics.dto.response;

import lombok.*;

/**
 * Response DTO for Dispute Ratio Statistics
 * Provides breakdown of disputes by status
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeRatioResponse {
    
    /**
     * Number of disputes with OPEN status
     */
    private long openCount;
    
    /**
     * Number of disputes with RESOLVED status
     */
    private long resolvedCount;
    
    /**
     * Number of disputes with REJECTED status
     */
    private long rejectedCount;
    
    /**
     * Total number of all disputes
     */
    private long total;
}
