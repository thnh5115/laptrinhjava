package ccm.admin.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String fullName,
        String password,
        @NotBlank String role,
        Boolean active
) {}