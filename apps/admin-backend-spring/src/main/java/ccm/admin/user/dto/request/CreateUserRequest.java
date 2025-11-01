package ccm.admin.user.dto.request;

import ccm.admin.user.validation.StrongPassword;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String fullName,
        @Size(min = 8, message = "Password must be at least 8 chars") 
        @StrongPassword 
        @NotBlank String password,
        @NotBlank String role // ADMIN/BUYER/EV_OWNER/AUDITOR
) {}
