package ccm.admin.system.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for updating a setting value
 * Admin can change setting values at runtime
 */
@Getter
@Setter
public class UpdateSettingRequest {
    
    /**
     * New value for the setting
     * Stored as string, parsed by application layer
     * Must not be blank
     */
    @NotBlank(message = "Value must not be blank")
    private String value;
}
