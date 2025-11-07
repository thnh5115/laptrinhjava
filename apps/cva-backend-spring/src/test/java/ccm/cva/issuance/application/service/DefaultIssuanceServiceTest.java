package ccm.cva.issuance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;
import ccm.cva.wallet.client.WalletClient;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultIssuanceServiceTest {

    private static final UUID REQUEST_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffffffff");
    private static final UUID OWNER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Mock
    private CreditIssuanceRepository repository;

    @Mock
    private WalletClient walletClient;

    private DefaultIssuanceService issuanceService;

    @BeforeEach
    void setUp() {
        issuanceService = new DefaultIssuanceService(repository, walletClient);
    }

    @Test
    void rejectsMissingIdempotencyKey() {
        VerificationRequest request = buildRequest();

        assertThatThrownBy(() -> issuanceService.issueCredits(request, " ", null))
            .isInstanceOf(DomainValidationException.class)
            .hasMessageContaining("Missing idempotency key");
    }

    @Test
    void createsIssuanceAndCreditsWalletOnFirstInvocation() {
        VerificationRequest request = buildRequest();
        when(repository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(repository.save(any(CreditIssuance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditIssuance issuance = issuanceService.issueCredits(request, "key-1", "corr-1");

        assertThat(issuance.getCreditsRounded()).isNotNull();
        verify(repository, times(1)).save(any(CreditIssuance.class));
        verify(walletClient).credit(OWNER_ID, issuance.getCreditsRounded(), "corr-1", "key-1");
    }

    @Test
    void returnsExistingIssuanceWhenIdempotencyReplayed() {
        VerificationRequest request = buildRequest();
        CreditIssuance existing = new CreditIssuance();
        existing.setId(UUID.randomUUID());
        existing.setVerificationRequest(request);
        existing.setOwnerId(OWNER_ID);
        existing.setCreditsRounded(new BigDecimal("12.34"));
        when(repository.findByIdempotencyKey("key-2")).thenReturn(Optional.of(existing));

        CreditIssuance issuance = issuanceService.issueCredits(request, "key-2", "corr-2");

        assertThat(issuance).isSameAs(existing);
        verify(repository, never()).save(any(CreditIssuance.class));
        verify(walletClient, never()).credit(any(), any(), any(), any());
    }

    @Test
    void throwsWhenIdempotencyBelongsToDifferentRequest() {
        VerificationRequest request = buildRequest();
        VerificationRequest otherRequest = buildRequest();
        otherRequest.setId(UUID.randomUUID());

        CreditIssuance existing = new CreditIssuance();
        existing.setVerificationRequest(otherRequest);
        when(repository.findByIdempotencyKey("key-3")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> issuanceService.issueCredits(request, "key-3", null))
            .isInstanceOf(DomainValidationException.class)
            .hasMessageContaining("Idempotency key already used");
    }

    private VerificationRequest buildRequest() {
        VerificationRequest request = new VerificationRequest();
        request.setId(REQUEST_ID);
        request.setOwnerId(OWNER_ID);
        request.setTripId("TRIP-123");
        request.setDistanceKm(new BigDecimal("120.0"));
        request.setEnergyKwh(new BigDecimal("24.0"));
        request.setChecksum("checksum-123");
        request.setStatus(VerificationStatus.PENDING);
        return request;
    }
}
