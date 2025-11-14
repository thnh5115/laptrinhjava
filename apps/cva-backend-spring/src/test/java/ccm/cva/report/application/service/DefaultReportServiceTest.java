package ccm.cva.report.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.credit.entity.enums.CreditStatus;
import ccm.admin.credit.repository.CarbonCreditRepository;
import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.cva.report.application.dto.CarbonAuditReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultReportServiceTest {

    @Mock
    private JourneyRepository journeyRepository;

    @Mock
    private CarbonCreditRepository carbonCreditRepository;

    private DefaultReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new DefaultReportService(journeyRepository, carbonCreditRepository);
    }

    @Test
    void buildReportIncludesIssuanceAndGeneratesPdf() {
        Journey journey = Journey.builder()
            .id(1L)
            .userId(42L)
            .journeyDate(LocalDate.of(2025, 11, 1))
            .startLocation("Origin")
            .endLocation("Destination")
            .distanceKm(new BigDecimal("140.0"))
            .energyUsedKwh(new BigDecimal("18.0"))
            .creditsGenerated(new BigDecimal("12.50"))
            .status(JourneyStatus.VERIFIED)
            .createdAt(LocalDateTime.now().minusDays(2))
            .verifiedAt(LocalDateTime.now().minusDays(1))
            .verifiedBy(9001L)
            .build();

        CarbonCredit credit = CarbonCredit.builder()
            .id(100L)
            .ownerId(journey.getUserId())
            .journeyId(journey.getId())
            .amount(new BigDecimal("12.50"))
            .status(CreditStatus.AVAILABLE)
            .pricePerCredit(new BigDecimal("3.25"))
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();

        when(journeyRepository.findById(journey.getId())).thenReturn(Optional.of(journey));
        when(carbonCreditRepository.findByJourneyId(journey.getId())).thenReturn(Optional.of(credit));

        CarbonAuditReport report = reportService.buildReport(journey.getId());
        assertThat(report.journeyId()).isEqualTo(journey.getId());
        assertThat(report.ownerId()).isEqualTo(journey.getUserId());
        assertThat(report.credit()).isNotNull();
        assertThat(report.signature()).isNotBlank();
        assertThat(report.generatedAt()).isNotNull();

        byte[] pdf = reportService.renderPdf(report);
        assertThat(pdf).isNotEmpty();
    }
}
