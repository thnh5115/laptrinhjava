package ccm.admin.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserStatusUpdateRequest(
    @NotBlank(message = "Status must not be blank")
    @Pattern(regexp = "^(ACTIVE|SUSPENDED|BANNED)$", 
             message = "Status must be one of: ACTIVE, SUSPENDED, BANNED")
    String status
) {
}
