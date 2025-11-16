package ccm.admin.audit.service;

import ccm.admin.audit.dto.response.AuditChartResponse;
import ccm.admin.audit.dto.response.AuditLogResponse;
import ccm.admin.audit.dto.response.AuditSummaryResponse;
import ccm.common.dto.paging.PageResponse;

/** service - Service Interface - Record and query audit logs */

public interface AuditLogService {
    
    PageResponse<AuditLogResponse> getAuditLogs(
            int page, int size, String sort,
            String keyword, String username
    );

    
    AuditSummaryResponse getSummary();

    
    AuditChartResponse getCharts(int days);
}
