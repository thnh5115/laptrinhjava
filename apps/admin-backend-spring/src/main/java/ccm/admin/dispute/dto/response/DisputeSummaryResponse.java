package ccm.admin.dispute.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Summary response for dispute list view
 * Contains essential information for table display
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeSummaryResponse {
    
    private Long id;
    
    private String disputeCode;
    
    private String raisedBy;
    
    private String status;
    
    private Long transactionId;
    
    private LocalDateTime createdAt;
}
