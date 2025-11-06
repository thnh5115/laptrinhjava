package ccm.buyer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotBlank(message = "status is required")
    @Pattern(
        regexp = "(?i)PENDING|PAID|APPROVED|REJECTED|CANCELLED",
        message = "status must be one of: PENDING, PAID, APPROVED, REJECTED, CANCELLED"
    )
    private String status;
}
