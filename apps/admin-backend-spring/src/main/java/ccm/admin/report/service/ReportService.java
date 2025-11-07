package ccm.admin.report.service;

import ccm.admin.report.dto.response.ReportChartResponse;
import ccm.admin.report.dto.response.ReportSummaryResponse;

/** service - Service Interface - service business logic and data operations */

public interface ReportService {
    
    
    ReportSummaryResponse getSummary();
    
    
    ReportChartResponse getMonthlyReport(int year);
}
