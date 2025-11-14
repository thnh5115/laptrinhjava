package ccm.cva.analytics.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.cva.analytics.application.dto.AnalyticsSeriesResponse;
import ccm.cva.analytics.application.dto.AnalyticsSummaryResponse;
import ccm.cva.analytics.application.dto.DailyRequestMetric;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    private JourneyRepository journeyRepository;
    private CreditIssuanceRepository issuanceRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
        analyticsService = new AnalyticsService(journeyRepository);
        analyticsService = new AnalyticsService(requestRepository, issuanceRepository);
    }

    @Test
        when(journeyRepository.count()).thenReturn(10L);
        when(journeyRepository.countByStatus(JourneyStatus.PENDING)).thenReturn(3L);
        when(journeyRepository.countByStatus(JourneyStatus.VERIFIED)).thenReturn(6L);
        when(journeyRepository.countByStatus(JourneyStatus.REJECTED)).thenReturn(1L);
        when(journeyRepository.calculateTotalCreditsGenerated()).thenReturn(new BigDecimal("42.780"));
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
        LocalDateTime day1 = from.atStartOfDay();
        LocalDateTime day2 = from.plusDays(1).atStartOfDay().plusHours(1);
        LocalDateTime day3 = from.plusDays(2).atStartOfDay().plusHours(2);

        Journey submittedDay1 = Journey.builder()
            .id(1L)
            .userId(101L)
            .journeyDate(from)
            .startLocation("City A")
            .endLocation("City B")
            .distanceKm(new BigDecimal("100.0"))
            .energyUsedKwh(new BigDecimal("30.0"))
            .status(JourneyStatus.PENDING)
            .createdAt(day1)
            .build();

        Journey approvedDay2 = Journey.builder()
            .id(2L)
            .userId(102L)
            .journeyDate(from)
            .startLocation("City A")
            .endLocation("City C")
            .distanceKm(new BigDecimal("120.0"))
            .energyUsedKwh(new BigDecimal("35.0"))
            .status(JourneyStatus.VERIFIED)
            .createdAt(day1)
            .verifiedAt(day2)
            .creditsGenerated(new BigDecimal("12.34"))
            .build();

        Journey rejectedDay3 = Journey.builder()
            .id(3L)
            .userId(103L)
            .journeyDate(from.plusDays(1))
            .startLocation("City D")
            .endLocation("City E")
            .distanceKm(new BigDecimal("90.0"))
            .energyUsedKwh(new BigDecimal("28.0"))
            .status(JourneyStatus.REJECTED)
            .createdAt(day2)
            .verifiedAt(day3)
            .build();

        when(journeyRepository.findAllByCreatedAtBetweenOrderByCreatedAtAsc(any(), any()))
            .thenReturn(List.of(submittedDay1, approvedDay2, rejectedDay3));
        when(journeyRepository.findAllByVerifiedAtBetweenOrderByVerifiedAtAsc(any(), any()))
            .thenReturn(List.of(approvedDay2, rejectedDay3));

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
