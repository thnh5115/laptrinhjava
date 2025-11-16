package ccm.cva.report.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import ccm.cva.report.application.dto.CarbonAuditReport;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.verification.application.command.ApproveVerificationRequestCommand;
import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.application.service.VerificationService;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import ccm.cva.wallet.client.WalletClient;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DefaultReportServiceTest {

    private static final UUID OWNER_ID = UUID.fromString("22222222-3333-4444-5555-666666666666");
    private static final UUID VERIFIER_ID = UUID.fromString("77777777-8888-9999-aaaa-bbbbbbbbbbbb");

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private CreditIssuanceRepository creditIssuanceRepository;

    @BeforeEach
    void setup() {
        creditIssuanceRepository.deleteAll();
        verificationRequestRepository.deleteAll();
    }

    @Test
    void buildReportIncludesIssuanceAndGeneratesPdf() {
        VerificationRequest request = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-REPORT",
            new BigDecimal("140.0"),
            new BigDecimal("18.0"),
            "checksum-report",
            "Initial submission"
        ));

        verificationService.approve(request.getId(), new ApproveVerificationRequestCommand(
            VERIFIER_ID,
            "Approved after validation",
            "idem-report",
            "corr-report"
        ));

        CarbonAuditReport report = reportService.buildReport(request.getId());
        assertThat(report.requestId()).isEqualTo(request.getId());
        assertThat(report.issuance()).isNotNull();
        assertThat(report.signature()).isNotBlank();
        assertThat(report.generatedAt()).isNotNull();

        byte[] pdf = reportService.renderPdf(report);
        assertThat(pdf).isNotEmpty();
    }

    @TestConfiguration
    static class WalletClientStubConfig {

        @Bean
        @Primary
        WalletClient walletClient() {
            return Mockito.mock(WalletClient.class);
        }
    }
}
