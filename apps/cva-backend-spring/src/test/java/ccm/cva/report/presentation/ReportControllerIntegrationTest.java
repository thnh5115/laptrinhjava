package ccm.cva.report.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.report.application.service.ReportService;
import ccm.cva.security.SecurityProperties;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("44444444-5555-6666-7777-888888888888");
    private static final UUID VERIFIER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private CreditIssuanceRepository creditIssuanceRepository;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private MockMvc mockMvc;

    private VerificationRequest seededRequest;

    @BeforeEach
    void setUp() {
        creditIssuanceRepository.deleteAll();
        verificationRequestRepository.deleteAll();

        seededRequest = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-REPORT-CTRL",
            new BigDecimal("123.4"),
            new BigDecimal("17.5"),
            "checksum-report-ctrl",
            "Controller integration"));

        verificationService.approve(seededRequest.getId(), new ApproveVerificationRequestCommand(
            VERIFIER_ID,
            "Ready for audit",
            "idem-report-ctrl",
            "corr-report-ctrl"));
    }

    @Test
    void fetchesJsonReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/cva/reports/" + seededRequest.getId())
                .with(httpBasic(securityProperties.getDevUser().getUsername(), securityProperties.getDevUser().getPassword())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value(seededRequest.getId().toString()))
            .andExpect(jsonPath("$.issuance.id").isNotEmpty())
            .andExpect(jsonPath("$.signature").isNotEmpty());
    }

    @Test
    void downloadsPdfReport() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/cva/reports/" + seededRequest.getId())
                .param("format", "pdf")
                .with(httpBasic(securityProperties.getDevUser().getUsername(), securityProperties.getDevUser().getPassword())))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment; filename=")))
            .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
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
