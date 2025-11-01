package ccm.admin.audit.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditChartResponse {
    private Map<String, Long> requestsByDay;
    private Map<String, Long> topEndpoints;
}
