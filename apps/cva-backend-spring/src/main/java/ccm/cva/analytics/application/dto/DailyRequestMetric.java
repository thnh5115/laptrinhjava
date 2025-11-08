package ccm.cva.analytics.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRequestMetric(
        LocalDate date,
        long submissions,
        long approvals,
        long rejections,
        BigDecimal creditsIssued
) {
}
