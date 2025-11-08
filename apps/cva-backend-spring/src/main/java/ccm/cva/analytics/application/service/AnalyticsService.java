package ccm.cva.analytics.application.service;

import ccm.cva.analytics.application.dto.AnalyticsOverviewResponse;
import ccm.cva.analytics.application.dto.AnalyticsSeriesResponse;
import ccm.cva.analytics.application.dto.AnalyticsSummaryResponse;
import ccm.cva.analytics.application.dto.DailyRequestMetric;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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

    private final VerificationRequestRepository requestRepository;
    private final CreditIssuanceRepository issuanceRepository;

    public AnalyticsService(
            VerificationRequestRepository requestRepository,
            CreditIssuanceRepository issuanceRepository
    ) {
        this.requestRepository = requestRepository;
        this.issuanceRepository = issuanceRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary() {
        long total = requestRepository.count();
        long pending = requestRepository.countByStatus(VerificationStatus.PENDING);
        long approved = requestRepository.countByStatus(VerificationStatus.APPROVED);
        long rejected = requestRepository.countByStatus(VerificationStatus.REJECTED);

        double approvalRate = total > 0 ? roundRatio((double) approved / total) : 0.0;
        double rejectionRate = total > 0 ? roundRatio((double) rejected / total) : 0.0;

        BigDecimal totalCreditsIssued = Optional.ofNullable(issuanceRepository.sumCreditsRounded())
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
        Instant windowStart = sanitizedFrom.atStartOfDay(UTC).toInstant();
        Instant windowEndInclusive = sanitizedTo.plusDays(1).atStartOfDay(UTC).toInstant().minusNanos(1);

        List<VerificationRequest> createdWithinWindow = requestRepository
            .findAllByCreatedAtBetweenOrderByCreatedAtAsc(windowStart, windowEndInclusive);
        List<VerificationRequest> decidedWithinWindow = requestRepository
            .findAllByVerifiedAtBetweenOrderByVerifiedAtAsc(windowStart, windowEndInclusive);
        List<CreditIssuance> issuancesWithinWindow = issuanceRepository
            .findAllByCreatedAtBetweenOrderByCreatedAtAsc(windowStart, windowEndInclusive);

        for (VerificationRequest request : createdWithinWindow) {
            LocalDate submissionDay = toDay(request.getCreatedAt());
            MutableMetric metric = metricsByDay.get(submissionDay);
            if (metric == null) {
                continue;
            }
            metric.submissions++;
        }

        for (VerificationRequest request : decidedWithinWindow) {
            if (request.getVerifiedAt() == null) {
                continue;
            }
            LocalDate decisionDay = toDay(request.getVerifiedAt());
            MutableMetric metric = metricsByDay.get(decisionDay);
            if (metric == null) {
                continue;
            }
            if (request.getStatus() == VerificationStatus.APPROVED) {
                metric.approvals++;
            } else if (request.getStatus() == VerificationStatus.REJECTED) {
                metric.rejections++;
            }
        }

        BigDecimal creditsInWindow = BigDecimal.ZERO;
        for (CreditIssuance issuance : issuancesWithinWindow) {
            LocalDate issuanceDay = toDay(issuance.getCreatedAt());
            MutableMetric metric = metricsByDay.get(issuanceDay);
            if (metric == null) {
                continue;
            }
            metric.creditsIssued = metric.creditsIssued.add(issuance.getCreditsRounded());
            creditsInWindow = creditsInWindow.add(issuance.getCreditsRounded());
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

    private LocalDate toDay(Instant instant) {
        return instant.atZone(UTC).toLocalDate();
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
