package ccm.admin.dispute.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Detailed response for individual dispute view
 * Contains all information including description and admin notes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
