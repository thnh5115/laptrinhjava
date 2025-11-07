package ccm.admin.report.controller;

import ccm.admin.report.dto.response.ReportChartResponse;
import ccm.admin.report.dto.response.ReportHistoryResponse;
import ccm.admin.report.dto.response.ReportSummaryResponse;
import ccm.admin.report.service.ReportExportService;
import ccm.admin.report.service.ReportHistoryService;
import ccm.admin.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
/**
 * Report - REST Controller - Admin endpoints for Report management
 */

public class ReportAdminController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;
    private final ReportHistoryService reportHistoryService;

    /**
     * GET /api/admin/reports/summary - perform operation
     */
    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryResponse> getSummary() {
        ReportSummaryResponse summary = reportService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/monthly")
    public ResponseEntity<ReportChartResponse> getMonthlyReport(
            @RequestParam(defaultValue = "2025") int year) {

        ReportChartResponse monthlyReport = reportService.getMonthlyReport(year);
        return ResponseEntity.ok(monthlyReport);
    }

    @GetMapping(value = "/transactions.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportTransactionsCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword
    ) {
        byte[] csv = reportExportService.exportTransactionsCSV(from, to, status, type, keyword);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/transactions.xlsx")
    public ResponseEntity<byte[]> exportTransactionsXLSX(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword
    ) {
        byte[] xlsx = reportExportService.exportTransactionsXLSX(from, to, status, type, keyword);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @GetMapping(value = "/transactions.pdf")
    public ResponseEntity<byte[]> exportTransactionsPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword
    ) {
        byte[] pdf = reportExportService.exportTransactionsPDF(from, to, status, type, keyword);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/users.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportUsersCSV(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword
    ) {
        byte[] csv = reportExportService.exportUsersCSV(status, role, keyword);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/users.xlsx")
    public ResponseEntity<byte[]> exportUsersXLSX(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword
    ) {
        byte[] xlsx = reportExportService.exportUsersXLSX(status, role, keyword);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @GetMapping(value = "/users.pdf")
    public ResponseEntity<byte[]> exportUsersPDF(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword
    ) {
        byte[] pdf = reportExportService.exportUsersPDF(status, role, keyword);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * GET /api/admin/reports/history - Get report generation history Returns
     * paginated list of all reports generated by admins
     */
    @GetMapping("/history")
    public ResponseEntity<Page<ReportHistoryResponse>> getReportHistory(
            @PageableDefault(page = 0, size = 20, sort = "generatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReportHistoryResponse> history = reportHistoryService.getReportHistory(pageable);
        return ResponseEntity.ok(history);
    }
}
