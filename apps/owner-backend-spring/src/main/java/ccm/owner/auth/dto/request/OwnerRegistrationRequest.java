package ccm.owner.auth.dto.request;

import ccm.admin.user.validation.StrongPassword;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Request DTO for EV Owner registration
 */
public class OwnerRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @StrongPassword
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
    private String fullName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    /**
     * Vehicle Information (Optional but recommended)
     */
    @Size(max = 100, message = "Vehicle make must not exceed 100 characters")
    private String vehicleMake;

    @Size(max = 100, message = "Vehicle model must not exceed 100 characters")
    private String vehicleModel;

    @Min(value = 2000, message = "Vehicle year must be 2000 or later")
    @Max(value = 2030, message = "Vehicle year must not exceed 2030")
    private Integer vehicleYear;

    @Size(max = 50, message = "License plate must not exceed 50 characters")
    private String licensePlate;

    /**
     * Terms and conditions acceptance
     */
    @NotNull(message = "You must accept the terms and conditions")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTerms;
}