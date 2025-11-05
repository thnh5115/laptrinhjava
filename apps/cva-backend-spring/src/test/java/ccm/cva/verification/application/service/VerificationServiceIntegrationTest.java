package ccm.cva.verification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import java.math.BigDecimal;
import java.util.Optional;
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

    @BeforeEach
    void cleanDatabase() {
        creditIssuanceRepository.deleteAll();
        verificationRequestRepository.deleteAll();
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
        Optional<CreditIssuance> issuance = issuanceService.getByIdempotencyKey("idem-123");
        assertThat(issuance).isPresent();
        verify(walletClient, times(1)).credit(OWNER_ID, issuance.orElseThrow().getCreditsRounded(), "corr-123", "idem-123");

        VerificationRequest replayed = verificationService.approve(request.getId(), approveCommand);
        assertThat(replayed.getStatus()).isEqualTo(VerificationStatus.APPROVED);
        verify(walletClient, times(1)).credit(OWNER_ID, issuance.orElseThrow().getCreditsRounded(), "corr-123", "idem-123");
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
    }
}
