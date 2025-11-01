package ccm.admin.audit.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditSummaryResponse {
    private long totalLogs;
    private long totalUsers;
    private long errorCount;
}
