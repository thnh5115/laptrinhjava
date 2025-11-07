package ccm.admin.audit.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Response model for response data */

public class AuditSummaryResponse {
    private long totalLogs;
    private long totalUsers;
    private long errorCount;
}
