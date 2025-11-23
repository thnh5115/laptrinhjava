package ccm.cva.report.application.service;

import ccm.cva.report.application.dto.CarbonAuditReport;


public interface ReportService {

    CarbonAuditReport buildReport(Long requestId);

    byte[] renderPdf(CarbonAuditReport report);
}
