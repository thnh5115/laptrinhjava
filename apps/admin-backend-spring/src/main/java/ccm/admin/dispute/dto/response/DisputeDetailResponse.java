package ccm.admin.dispute.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class DisputeDetailResponse {
    
    private Long id;
    
    private String disputeCode;
    
    private String raisedBy;
    
    private String description;
    
    private String adminNote;
    
    private String status;
    
    private Long transactionId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
