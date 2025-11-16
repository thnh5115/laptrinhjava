package ccm.admin.report.service;

import ccm.admin.report.dto.response.ReportHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportHistoryService {
    
    /**
     * Get report generation history
     * @param pageable Pagination parameters
     * @return Page of report history
     */
    Page<ReportHistoryResponse> getReportHistory(Pageable pageable);
}
