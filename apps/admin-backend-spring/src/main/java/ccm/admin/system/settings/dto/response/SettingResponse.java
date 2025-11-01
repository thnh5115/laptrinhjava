package ccm.admin.system.settings.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for Setting entity
 * Used for GET endpoints to return setting information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingResponse {
    
    private Long id;
    
    private String keyName;
    
    private String value;
    
    private String description;
    
    private LocalDateTime updatedAt;
}
