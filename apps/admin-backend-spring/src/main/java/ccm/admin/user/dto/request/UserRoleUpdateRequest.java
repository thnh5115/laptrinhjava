package ccm.admin.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRoleUpdateRequest(
    @NotBlank(message = "Role must not be blank")
    @Pattern(regexp = "^(ADMIN|AUDITOR|BUYER|EV_OWNER)$", 
             message = "Role must be one of: ADMIN, AUDITOR, BUYER, EV_OWNER")
    String role
) {
}
