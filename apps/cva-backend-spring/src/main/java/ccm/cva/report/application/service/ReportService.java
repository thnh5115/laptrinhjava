package ccm.cva.report.application.service;

import ccm.cva.report.application.dto.CarbonAuditReport;

public interface ReportService {

    CarbonAuditReport buildReport(Long journeyId);

    byte[] renderPdf(CarbonAuditReport report);
}
