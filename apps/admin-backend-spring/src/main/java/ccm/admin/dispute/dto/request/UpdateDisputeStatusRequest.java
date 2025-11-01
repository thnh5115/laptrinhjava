package ccm.admin.dispute.dto.request;

import ccm.admin.dispute.entity.enums.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for updating dispute status
 * Admin can change status and add notes
 */
@Getter
@Setter
public class UpdateDisputeStatusRequest {
    
    /**
     * New status for the dispute
     */
    @NotNull(message = "Status is required")
    private DisputeStatus status;
    
    /**
     * Admin's note/response to the dispute
     */
    @Size(max = 1000, message = "Admin note must not exceed 1000 characters")
    private String adminNote;
}
