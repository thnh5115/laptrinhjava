package ccm.cva.report.presentation;

import ccm.cva.security.RateLimited;
import ccm.cva.report.application.dto.CarbonAuditReport;
import ccm.cva.report.application.service.ReportFormat;
import ccm.cva.report.application.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // <--- THÊM DÒNG NÀY

@RestController
@RequestMapping("/api/cva/reports")
@Tag(name = "CVA Reports", description = "Carbon audit report generation")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Generate carbon audit report")
    @GetMapping("/{id}")
    @RateLimited("report")
    public ResponseEntity<?> generateReport(
            @PathVariable Long id,
            @Parameter(description = "Output format (json or pdf)")
            @RequestParam(name = "format", required = false, defaultValue = "json") String format
    ) {
        ReportFormat reportFormat = ReportFormat.from(format);
        CarbonAuditReport report = reportService.buildReport(id);

        if (reportFormat == ReportFormat.PDF) {
            byte[] pdf = reportService.renderPdf(report);
            String filename = "cva-report-" + report.requestId() + ".pdf";
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(pdf);
        }

        return ResponseEntity.ok(report);
    }
}
