package ccm.cva.verification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ccm.cva.audit.client.AuditLogClient;
import ccm.cva.issuance.application.service.IssuanceService;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.verification.application.command.ApproveVerificationRequestCommand;
import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.application.command.RejectVerificationRequestCommand;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import ccm.cva.wallet.client.WalletClient;
import ccm.cva.verification.application.query.VerificationRequestQuery;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class VerificationServiceIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("99999999-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID VERIFIER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private IssuanceService issuanceService;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private WalletClient walletClient;

    @Autowired
    private CreditIssuanceRepository creditIssuanceRepository;

    @Autowired
    private AuditLogClient auditLogClient;

    @BeforeEach
    void cleanDatabase() {
        creditIssuanceRepository.deleteAll();
        verificationRequestRepository.deleteAll();
        Mockito.reset(walletClient, auditLogClient);
    }

    @Test
    void approveFlowCreatesIssuanceAndCreditsWallet() {
        VerificationRequest request = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-APPROVE",
            new BigDecimal("120.0"),
            new BigDecimal("24.0"),
            "checksum-approve",
            null
        ));

        ApproveVerificationRequestCommand approveCommand = new ApproveVerificationRequestCommand(
            VERIFIER_ID,
            "Looks good",
            "idem-123",
            "corr-123"
        );

        VerificationRequest approved = verificationService.approve(request.getId(), approveCommand);

        assertThat(approved.getStatus()).isEqualTo(VerificationStatus.APPROVED);
        assertThat(approved.getVerifiedAt()).isNotNull();
    assertThat(approved.getCreditIssuance()).isNotNull();
        Optional<CreditIssuance> issuance = issuanceService.getByIdempotencyKey("idem-123");
        assertThat(issuance).isPresent();
        verify(walletClient, times(1)).credit(OWNER_ID, issuance.orElseThrow().getCreditsRounded(), "corr-123", "idem-123");
        verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.created"), Mockito.anyMap());
        ArgumentCaptor<Map<String, Object>> approvedPayload = mapCaptor();
        Mockito.verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.approved"), approvedPayload.capture());
        Map<String, Object> payload = approvedPayload.getValue();
        assertThat(payload.get("requestId")).isEqualTo(request.getId());
        assertThat(payload.get("issuanceId")).isEqualTo(issuance.orElseThrow().getId());
        assertThat(payload.get("idempotencyKey")).isEqualTo("idem-123");
        assertThat(payload.get("correlationId")).isEqualTo("corr-123");
        assertThat(payload.get("notes")).isEqualTo("Looks good");

        VerificationRequest replayed = verificationService.approve(request.getId(), approveCommand);
        assertThat(replayed.getStatus()).isEqualTo(VerificationStatus.APPROVED);
        verify(walletClient, times(1)).credit(OWNER_ID, issuance.orElseThrow().getCreditsRounded(), "corr-123", "idem-123");
        Mockito.verifyNoMoreInteractions(auditLogClient);
    }

    @Test
    void rejectFlowUpdatesStatus() {
        VerificationRequest request = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-REJECT",
            new BigDecimal("100.0"),
            new BigDecimal("22.0"),
            "checksum-reject",
            null
        ));

        RejectVerificationRequestCommand command = new RejectVerificationRequestCommand(
            VERIFIER_ID,
            "Data mismatch"
        );

        VerificationRequest rejected = verificationService.reject(request.getId(), command);
        assertThat(rejected.getStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(rejected.getVerifierId()).isEqualTo(VERIFIER_ID);
        verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.created"), Mockito.anyMap());
        ArgumentCaptor<Map<String, Object>> rejectedPayload = mapCaptor();
        Mockito.verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.rejected"), rejectedPayload.capture());
        Map<String, Object> payload = rejectedPayload.getValue();
        assertThat(payload.get("reason")).isEqualTo("Data mismatch");
        Mockito.verifyNoMoreInteractions(auditLogClient);
    }

    @Test
    void approveTrimsIdentifiersAndNotesBeforePersisting() {
        VerificationRequest request = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-WHITESPACE",
            new BigDecimal("80.0"),
            new BigDecimal("16.0"),
            "checksum-whitespace",
            "  queued   "
        ));

        ApproveVerificationRequestCommand approveCommand = new ApproveVerificationRequestCommand(
            VERIFIER_ID,
            "   Approved cleanly   ",
            "  idem-spaces  ",
            "  corr-spaces  "
        );

        VerificationRequest approved = verificationService.approve(request.getId(), approveCommand);

        assertThat(approved.getNotes()).isEqualTo("Approved cleanly");
        assertThat(approved.getCreditIssuance()).isNotNull();
        CreditIssuance issuance = approved.getCreditIssuance();
        assertThat(issuance.getIdempotencyKey()).isEqualTo("idem-spaces");
        assertThat(issuance.getCorrelationId()).isEqualTo("corr-spaces");

    verify(walletClient, times(1)).credit(OWNER_ID, issuance.getCreditsRounded(), "corr-spaces", "idem-spaces");
    Mockito.verifyNoMoreInteractions(walletClient);
    ArgumentCaptor<Map<String, Object>> approvedPayload = mapCaptor();
    Mockito.verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.created"), Mockito.anyMap());
    Mockito.verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.approved"), approvedPayload.capture());
        Map<String, Object> audit = approvedPayload.getValue();
        assertThat(audit.get("idempotencyKey")).isEqualTo("idem-spaces");
        assertThat(audit.get("correlationId")).isEqualTo("corr-spaces");
        assertThat(audit.get("notes")).isEqualTo("Approved cleanly");
        Mockito.verifyNoMoreInteractions(auditLogClient);
    }

    @Test
    void rejectTrimsReasonBeforePersisting() {
        VerificationRequest request = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-REJECT-WHITE",
            new BigDecimal("50.0"),
            new BigDecimal("10.0"),
            "checksum-reject-white",
            null
        ));

        RejectVerificationRequestCommand command = new RejectVerificationRequestCommand(
            VERIFIER_ID,
            "   Out of range   "
        );

        VerificationRequest rejected = verificationService.reject(request.getId(), command);
        assertThat(rejected.getNotes()).isEqualTo("Out of range");

        ArgumentCaptor<Map<String, Object>> payload = mapCaptor();
    Mockito.verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.created"), Mockito.anyMap());
    Mockito.verify(auditLogClient, times(1)).record(Mockito.eq("cva.request.rejected"), payload.capture());
        assertThat(payload.getValue().get("reason")).isEqualTo("Out of range");
        Mockito.verifyNoInteractions(walletClient);
        Mockito.verifyNoMoreInteractions(auditLogClient);
    }

    @Test
    void searchSupportsFilteringByStatusAndText() {
        verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-PENDING",
            new BigDecimal("80.0"),
            new BigDecimal("16.0"),
            "checksum-pending",
            null
        ));

        VerificationRequest approved = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-APPROVED",
            new BigDecimal("100.0"),
            new BigDecimal("20.0"),
            "checksum-approved",
            null
        ));

        verificationService.approve(approved.getId(), new ApproveVerificationRequestCommand(
            VERIFIER_ID,
            null,
            "idem-search",
            "corr-search"
        ));

        VerificationRequestQuery query = new VerificationRequestQuery(VerificationStatus.APPROVED, null, null, null, "trip-approved");
        Page<VerificationRequest> page = verificationService.search(query, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    VerificationRequest result = page.getContent().get(0);
        assertThat(result.getCreditIssuance()).isNotNull();
        assertThat(result.getTripId()).isEqualTo("TRIP-APPROVED");
    }

    @Test
    void approveWithoutIdempotencyKeyFailsFast() {
        VerificationRequest request = verificationService.create(new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-FAIL",
            new BigDecimal("90.0"),
            new BigDecimal("18.0"),
            "checksum-fail",
            null
        ));

        ApproveVerificationRequestCommand command = new ApproveVerificationRequestCommand(
            VERIFIER_ID,
            null,
            " ",
            null
        );

        assertThatThrownBy(() -> verificationService.approve(request.getId(), command))
            .isInstanceOf(DomainValidationException.class)
            .hasMessageContaining("Missing idempotency key");
    }

    @TestConfiguration
    static class WalletClientTestConfig {

        @Bean
        @Primary
        WalletClient walletClient() {
            return Mockito.mock(WalletClient.class);
        }

        @Bean
        @Primary
        AuditLogClient auditLogClient() {
            return Mockito.mock(AuditLogClient.class);
        }
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
    }
}
