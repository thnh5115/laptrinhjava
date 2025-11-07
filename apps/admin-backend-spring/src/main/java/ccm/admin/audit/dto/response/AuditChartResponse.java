package ccm.admin.audit.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class AuditChartResponse {
    private Map<String, Long> requestsByDay;
    private Map<String, Long> topEndpoints;
}
