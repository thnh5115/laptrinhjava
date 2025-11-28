package ccm.owner.report.controller;

import ccm.owner.report.dto.response.OwnerReportSummaryResponse;
import ccm.owner.report.dto.response.OwnerMonthlyReportResponse;
import ccm.owner.report.service.OwnerReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/reports")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('EV_OWNER')")
@Tag(name = "EV Owner - Reports", description = "Personal reports for EV Owners")
public class OwnerReportController {

    private final OwnerReportService reportService;

    @Operation(
            summary = "Get Summary Report",
            description = "Get comprehensive summary of journeys, credits, and earnings"
    )
    @GetMapping("/summary")
    public ResponseEntity<OwnerReportSummaryResponse> getSummary() {
        log.info("Getting summary report for current user");
        OwnerReportSummaryResponse summary = reportService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Get Monthly Report",
            description = "Get monthly breakdown of journeys, credits, and earnings for a specific year"
    )
    @GetMapping("/monthly")
    public ResponseEntity<OwnerMonthlyReportResponse> getMonthlyReport(
            @RequestParam(defaultValue = "2025") int year) {

        log.info("Getting monthly report for year: {}", year);
        OwnerMonthlyReportResponse report = reportService.getMonthlyReport(year);
        return ResponseEntity.ok(report);
    }
}