package ccm.admin.system.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** request - Request DTO - Request payload for request operations */

public class UpdateSettingRequest {
    
    
    @NotBlank(message = "Value must not be blank")
    private String value;
}
