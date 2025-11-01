package ccm.admin.listing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for updating listing status
 */
@Data
public class ListingStatusUpdateRequest {
    @NotBlank(message = "Status must not be blank")
    @Pattern(regexp = "^(APPROVED|REJECTED|PENDING)$", 
             message = "Status must be one of: APPROVED, REJECTED, PENDING")
    private String status; // APPROVED or REJECTED
}
