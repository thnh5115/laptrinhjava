package ccm.admin.audit.service;

import ccm.admin.audit.dto.response.AuditChartResponse;
import ccm.admin.audit.dto.response.AuditLogResponse;
import ccm.admin.audit.dto.response.AuditSummaryResponse;
import ccm.common.dto.paging.PageResponse;

public interface AuditLogService {
    /**
     * Get paginated audit logs with filtering and sorting
     */
    PageResponse<AuditLogResponse> getAuditLogs(
            int page, int size, String sortBy, String direction,
            String keyword, String username
    );

    /**
     * Get audit summary statistics
     */
    AuditSummaryResponse getSummary();

    /**
     * Get audit charts data for dashboard
     */
    AuditChartResponse getCharts(int days);
}
