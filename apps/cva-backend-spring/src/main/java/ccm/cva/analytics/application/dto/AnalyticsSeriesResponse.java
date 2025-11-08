package ccm.cva.analytics.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Response envelope for the analytics time-series endpoint.
 */
public record AnalyticsSeriesResponse(
        LocalDate from,
        LocalDate to,
        List<DailyRequestMetric> data
) {
}
