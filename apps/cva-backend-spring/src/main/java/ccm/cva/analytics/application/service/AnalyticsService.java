package ccm.cva.analytics.application.service;

import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.cva.analytics.application.dto.AnalyticsOverviewResponse;
import ccm.cva.analytics.application.dto.AnalyticsSeriesResponse;
import ccm.cva.analytics.application.dto.AnalyticsSummaryResponse;
import ccm.cva.analytics.application.dto.DailyRequestMetric;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private final JourneyRepository journeyRepository;

    public AnalyticsService(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary() {
        long total = journeyRepository.count();
        long pending = journeyRepository.countByStatus(JourneyStatus.PENDING);
        long approved = journeyRepository.countByStatus(JourneyStatus.VERIFIED);
        long rejected = journeyRepository.countByStatus(JourneyStatus.REJECTED);

        double approvalRate = total > 0 ? roundRatio((double) approved / total) : 0.0;
        double rejectionRate = total > 0 ? roundRatio((double) rejected / total) : 0.0;

        BigDecimal totalCreditsIssued = Optional.ofNullable(journeyRepository.calculateTotalCreditsGenerated())
            .orElse(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_EVEN);

        return new AnalyticsSummaryResponse(
            total,
            pending,
            approved,
            rejected,
            approvalRate,
            rejectionRate,
            totalCreditsIssued
        );
    }

    public AnalyticsSeriesResponse getSeries(LocalDate from, LocalDate to) {
        LocalDate sanitizedFrom = from != null ? from : LocalDate.now(UTC).minusDays(29);
        LocalDate sanitizedTo = to != null ? to : LocalDate.now(UTC);
        if (sanitizedFrom.isAfter(sanitizedTo)) {
            LocalDate swap = sanitizedFrom;
            sanitizedFrom = sanitizedTo;
            sanitizedTo = swap;
        }

        Map<LocalDate, MutableMetric> metricsByDay = initialiseTimeline(sanitizedFrom, sanitizedTo);
        LocalDateTime windowStart = sanitizedFrom.atStartOfDay();
        LocalDateTime windowEndInclusive = sanitizedTo.plusDays(1).atStartOfDay().minusNanos(1);

        List<Journey> createdWithinWindow = journeyRepository
            .findAllByCreatedAtBetweenOrderByCreatedAtAsc(windowStart, windowEndInclusive);
        List<Journey> decidedWithinWindow = journeyRepository
            .findAllByVerifiedAtBetweenOrderByVerifiedAtAsc(windowStart, windowEndInclusive);

        for (Journey request : createdWithinWindow) {
            if (request.getCreatedAt() == null) {
                continue;
            }
            LocalDate submissionDay = request.getCreatedAt().toLocalDate();
            MutableMetric metric = metricsByDay.get(submissionDay);
            if (metric == null) {
                continue;
            }
            metric.submissions++;
        }

        for (Journey request : decidedWithinWindow) {
            if (request.getVerifiedAt() == null) {
                continue;
            }
            LocalDate decisionDay = request.getVerifiedAt().toLocalDate();
            MutableMetric metric = metricsByDay.get(decisionDay);
            if (metric == null) {
                continue;
            }
            if (request.getStatus() == JourneyStatus.VERIFIED) {
                metric.approvals++;
            } else if (request.getStatus() == JourneyStatus.REJECTED) {
                metric.rejections++;
            }
        }

        BigDecimal creditsInWindow = BigDecimal.ZERO;
        for (Journey request : decidedWithinWindow) {
            if (request.getVerifiedAt() == null || request.getStatus() != JourneyStatus.VERIFIED) {
                continue;
            }
            LocalDate decisionDay = request.getVerifiedAt().toLocalDate();
            MutableMetric metric = metricsByDay.get(decisionDay);
            if (metric == null) {
                continue;
            }
            BigDecimal credits = Optional.ofNullable(request.getCreditsGenerated()).orElse(BigDecimal.ZERO);
            metric.creditsIssued = metric.creditsIssued.add(credits);
            creditsInWindow = creditsInWindow.add(credits);
        }

        List<DailyRequestMetric> timeline = metricsByDay.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new DailyRequestMetric(
                entry.getKey(),
                entry.getValue().submissions,
                entry.getValue().approvals,
                entry.getValue().rejections,
                entry.getValue().creditsIssued.setScale(2, RoundingMode.HALF_EVEN)
            ))
            .collect(Collectors.toCollection(ArrayList::new));

        return new AnalyticsSeriesResponse(sanitizedFrom, sanitizedTo, timeline);
    }

    public AnalyticsOverviewResponse buildOverview(int windowDays) {
        int sanitizedWindow = Math.max(7, Math.min(windowDays, 90));
        LocalDate today = LocalDate.now(UTC);
        LocalDate from = today.minusDays(sanitizedWindow - 1);

        AnalyticsSummaryResponse summary = getSummary();
        AnalyticsSeriesResponse series = getSeries(from, today);

        BigDecimal creditsInWindow = series.data().stream()
            .map(DailyRequestMetric::creditsIssued)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_EVEN);

        long requestsInWindow = series.data().stream()
            .mapToLong(DailyRequestMetric::submissions)
            .sum();

        return new AnalyticsOverviewResponse(
            summary.totalRequests(),
            summary.pendingRequests(),
            summary.approvedRequests(),
            summary.rejectedRequests(),
            summary.approvalRate(),
            summary.rejectionRate(),
            summary.totalCreditsIssued(),
            creditsInWindow,
            requestsInWindow,
            series.data()
        );
    }

    private Map<LocalDate, MutableMetric> initialiseTimeline(LocalDate from, LocalDate to) {
        Map<LocalDate, MutableMetric> metrics = new LinkedHashMap<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            metrics.put(cursor, new MutableMetric());
            cursor = cursor.plusDays(1);
        }
        return metrics;
    }

    private double roundRatio(double ratio) {
        return Math.round(ratio * 1000.0) / 10.0; // one decimal place percentage
    }

    private static final class MutableMetric {
        long submissions = 0;
        long approvals = 0;
        long rejections = 0;
        BigDecimal creditsIssued = BigDecimal.ZERO;
    }
}
