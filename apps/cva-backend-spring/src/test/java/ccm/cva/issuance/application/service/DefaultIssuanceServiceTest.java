package ccm.cva.issuance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ccm.cva.issuance.application.query.CreditIssuanceQuery;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.shared.outbox.OutboxService;
import ccm.cva.shared.outbox.WalletCreditOutboxPayload;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;
import ccm.cva.wallet.client.WalletClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.support.RetryTemplate;

@ExtendWith(MockitoExtension.class)
class DefaultIssuanceServiceTest {

    private static final UUID REQUEST_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffffffff");
    private static final UUID OWNER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Mock
    private CreditIssuanceRepository repository;

    @Mock
    private WalletClient walletClient;

    @Mock
    private OutboxService outboxService;

    private DefaultIssuanceService issuanceService;

    private RetryTemplate retryTemplate;

    @BeforeEach
    void setUp() {
        retryTemplate = RetryTemplate.builder().maxAttempts(1).fixedBackoff(0).retryOn(Exception.class).build();
        issuanceService = new DefaultIssuanceService(repository, walletClient, retryTemplate, outboxService);
    }

    @AfterEach
    void tearDown() {
        Mockito.verifyNoMoreInteractions(outboxService);
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
    Mockito.verifyNoInteractions(outboxService);
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
    Mockito.verifyNoInteractions(outboxService);
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

    @Test
    void enqueuesOutboxWhenWalletFails() {
        VerificationRequest request = buildRequest();
        when(repository.findByIdempotencyKey("key-outbox")).thenReturn(Optional.empty());
        when(repository.save(any(CreditIssuance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.doThrow(new IllegalStateException("wallet down"))
            .when(walletClient)
            .credit(eq(OWNER_ID), any(BigDecimal.class), eq("corr-out"), eq("key-outbox"));

        CreditIssuance issuance = issuanceService.issueCredits(request, "key-outbox", "corr-out");

        assertThat(issuance).isNotNull();
        ArgumentCaptor<WalletCreditOutboxPayload> payloadCaptor = ArgumentCaptor.forClass(WalletCreditOutboxPayload.class);
        verify(outboxService).enqueueWalletCredit(payloadCaptor.capture(), eq("corr-out"), eq("key-outbox"));
        WalletCreditOutboxPayload payload = payloadCaptor.getValue();
        assertThat(payload.ownerId()).isEqualTo(OWNER_ID);
        assertThat(payload.idempotencyKey()).isEqualTo("key-outbox");
    }

    @Test
    void searchDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<CreditIssuance> expected = new PageImpl<>(List.of());
    when(repository.findAll(ArgumentMatchers.<Specification<CreditIssuance>>any(), eq(pageable))).thenReturn(expected);

        Page<CreditIssuance> result = issuanceService.search(new CreditIssuanceQuery(null, null, null, null), pageable);

        assertThat(result).isSameAs(expected);
    verify(repository).findAll(ArgumentMatchers.<Specification<CreditIssuance>>any(), eq(pageable));
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
