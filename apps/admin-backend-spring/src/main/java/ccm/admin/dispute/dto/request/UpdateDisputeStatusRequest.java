package ccm.admin.dispute.dto.request;

import ccm.admin.dispute.entity.enums.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** request - Request DTO - Request payload for request operations */

public class UpdateDisputeStatusRequest {
    
    
    @NotNull(message = "Status is required")
    private DisputeStatus status;
    
    
    @Size(max = 1000, message = "Admin note must not exceed 1000 characters")
    private String adminNote;
}
