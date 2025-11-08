package ccm.cva.analytics.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ccm.cva.analytics.application.dto.AnalyticsSeriesResponse;
import ccm.cva.analytics.application.dto.AnalyticsSummaryResponse;
import ccm.cva.analytics.application.dto.DailyRequestMetric;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private VerificationRequestRepository requestRepository;

    @Mock
    private CreditIssuanceRepository issuanceRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(requestRepository, issuanceRepository);
    }

    @Test
    void getSummaryAggregatesCounters() {
        when(requestRepository.count()).thenReturn(10L);
        when(requestRepository.countByStatus(VerificationStatus.PENDING)).thenReturn(3L);
        when(requestRepository.countByStatus(VerificationStatus.APPROVED)).thenReturn(6L);
        when(requestRepository.countByStatus(VerificationStatus.REJECTED)).thenReturn(1L);
        when(issuanceRepository.sumCreditsRounded()).thenReturn(new BigDecimal("42.780"));

        AnalyticsSummaryResponse summary = analyticsService.getSummary();

        assertThat(summary.totalRequests()).isEqualTo(10L);
        assertThat(summary.pendingRequests()).isEqualTo(3L);
        assertThat(summary.approvedRequests()).isEqualTo(6L);
        assertThat(summary.rejectedRequests()).isEqualTo(1L);
        assertThat(summary.approvalRate()).isEqualTo(60.0);
        assertThat(summary.rejectionRate()).isEqualTo(10.0);
        assertThat(summary.totalCreditsIssued()).isEqualByComparingTo("42.78");
    }

    @Test
    void getSeriesBuildsDailyTimeline() {
        LocalDate from = LocalDate.of(2025, 11, 1);
        LocalDate to = LocalDate.of(2025, 11, 3);
        Instant day1 = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant day2 = from.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).plusSeconds(3600);
        Instant day3 = from.plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC).plusSeconds(7200);

        VerificationRequest submittedDay1 = new VerificationRequest();
        submittedDay1.setId(UUID.randomUUID());
        submittedDay1.setCreatedAt(day1);
        submittedDay1.setStatus(VerificationStatus.PENDING);

        VerificationRequest approvedDay2 = new VerificationRequest();
        approvedDay2.setId(UUID.randomUUID());
        approvedDay2.setCreatedAt(day1);
        approvedDay2.setVerifiedAt(day2);
        approvedDay2.setStatus(VerificationStatus.APPROVED);

        VerificationRequest rejectedDay3 = new VerificationRequest();
        rejectedDay3.setId(UUID.randomUUID());
        rejectedDay3.setCreatedAt(day2);
        rejectedDay3.setVerifiedAt(day3);
        rejectedDay3.setStatus(VerificationStatus.REJECTED);

        CreditIssuance issuance = new CreditIssuance();
        issuance.setId(UUID.randomUUID());
        issuance.setCreatedAt(day2);
        issuance.setCreditsRounded(new BigDecimal("12.34"));

        when(requestRepository.findAllByCreatedAtBetweenOrderByCreatedAtAsc(any(), any()))
            .thenReturn(List.of(submittedDay1, approvedDay2, rejectedDay3));
        when(requestRepository.findAllByVerifiedAtBetweenOrderByVerifiedAtAsc(any(), any()))
            .thenReturn(List.of(approvedDay2, rejectedDay3));
        when(issuanceRepository.findAllByCreatedAtBetweenOrderByCreatedAtAsc(any(), any()))
            .thenReturn(List.of(issuance));
        when(issuanceRepository.sumCreditsRounded()).thenReturn(BigDecimal.ZERO);

        AnalyticsSeriesResponse series = analyticsService.getSeries(from, to);

        assertThat(series.data()).hasSize(3);
        DailyRequestMetric dayOneMetric = series.data().get(0);
        assertThat(dayOneMetric.date()).isEqualTo(from);
        assertThat(dayOneMetric.submissions()).isEqualTo(2);
        assertThat(dayOneMetric.approvals()).isZero();
        assertThat(dayOneMetric.rejections()).isZero();

        DailyRequestMetric dayTwoMetric = series.data().get(1);
        assertThat(dayTwoMetric.approvals()).isEqualTo(1);
        assertThat(dayTwoMetric.creditsIssued()).isEqualByComparingTo("12.34");

        DailyRequestMetric dayThreeMetric = series.data().get(2);
        assertThat(dayThreeMetric.rejections()).isEqualTo(1);
    }
}
