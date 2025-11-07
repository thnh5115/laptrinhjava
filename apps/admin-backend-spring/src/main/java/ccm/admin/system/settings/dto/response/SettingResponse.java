package ccm.admin.system.settings.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class SettingResponse {
    
    private Long id;
    
    private String keyName;
    
    private String value;
    
    private String description;
    
    private LocalDateTime updatedAt;
}
