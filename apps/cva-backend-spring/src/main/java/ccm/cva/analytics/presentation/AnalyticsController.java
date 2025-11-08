package ccm.cva.analytics.presentation;

import ccm.cva.analytics.application.dto.AnalyticsOverviewResponse;
import ccm.cva.analytics.application.dto.AnalyticsSeriesResponse;
import ccm.cva.analytics.application.dto.AnalyticsSummaryResponse;
import ccm.cva.analytics.application.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/cva/analytics")
@Validated
@Tag(name = "CVA Analytics", description = "Portfolio metrics for CVA dashboard")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Fetch analytics overview", description = "Returns aggregated metrics for the specified time window (7-90 days)")
    @GetMapping("/overview")
    public AnalyticsOverviewResponse getOverview(
            @Parameter(description = "Number of days to include in the rolling window", example = "30")
            @RequestParam(name = "windowDays", required = false, defaultValue = "30")
            @Min(7) @Max(90) int windowDays
    ) {
        return analyticsService.buildOverview(windowDays);
    }

    @Operation(summary = "Fetch analytics summary", description = "Aggregated counters powering CVA dashboard tiles")
    @GetMapping("/summary")
    public AnalyticsSummaryResponse getSummary() {
        return analyticsService.getSummary();
    }

    @Operation(summary = "Fetch analytics time series", description = "Returns daily submissions, approvals, rejections, and credits within the requested range")
    @GetMapping("/series")
    public AnalyticsSeriesResponse getSeries(
            @Parameter(description = "Start date (inclusive)", example = "2025-11-01")
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive)", example = "2025-11-08")
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return analyticsService.getSeries(from, to);
    }
}
