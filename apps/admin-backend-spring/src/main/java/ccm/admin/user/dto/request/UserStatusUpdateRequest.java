package ccm.admin.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for updating user account status
 * 
 * Valid status values:
 * - ACTIVE: User can login and use the system
 * - SUSPENDED: Temporarily blocked, cannot login
 * - BANNED: Permanently blocked
 * 
 * Example JSON:
 * {
 *   "status": "ACTIVE"
 * }
 */
public record UserStatusUpdateRequest(
    @NotBlank(message = "Status must not be blank")
    @Pattern(regexp = "^(ACTIVE|SUSPENDED|BANNED)$", 
             message = "Status must be one of: ACTIVE, SUSPENDED, BANNED")
    String status
) {
}
