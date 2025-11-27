package ccm.admin.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    private String password;

    @NotBlank
    private String role; // Ví dụ: "BUYER", "EV_OWNER"
}