package ccm.admin.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for updating user role
 * 
 * Valid role values (case-insensitive):
 * - ADMIN: Full system access
 * - AUDITOR: Read-only audit access
 * - BUYER: Can buy carbon credits
 * - EV_OWNER: Electric vehicle owner, can sell credits
 * 
 * Example JSON:
 * {
 *   "role": "BUYER"
 * }
 */
public record UserRoleUpdateRequest(
    @NotBlank(message = "Role must not be blank")
    @Pattern(regexp = "^(ADMIN|AUDITOR|BUYER|EV_OWNER)$", 
             message = "Role must be one of: ADMIN, AUDITOR, BUYER, EV_OWNER")
    String role
) {
}
