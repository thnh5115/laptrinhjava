package ccm.cva.report.application.service;

import ccm.cva.report.application.dto.CarbonAuditReport;
import java.util.UUID;

public interface ReportService {

    CarbonAuditReport buildReport(UUID requestId);

    byte[] renderPdf(CarbonAuditReport report);
}
