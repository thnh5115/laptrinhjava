package ccm.cva.issuance.application.service;

import ccm.cva.issuance.application.query.CreditIssuanceQuery;
import ccm.cva.issuance.application.query.CreditIssuanceSpecifications;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.shared.outbox.OutboxService;
import ccm.cva.shared.outbox.WalletCreditOutboxPayload;
import ccm.cva.shared.trace.CorrelationIdHolder;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.wallet.client.WalletClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class DefaultIssuanceService implements IssuanceService {

    private final CreditIssuanceRepository repository;
    private final WalletClient walletClient;
    private final RetryTemplate externalRetryTemplate;
    private final OutboxService outboxService;

    private static final Logger log = LoggerFactory.getLogger(DefaultIssuanceService.class);

    private static final BigDecimal ICE_EMISSION_FACTOR_KG_PER_KM = new BigDecimal("0.120");
    private static final BigDecimal GRID_EMISSION_FACTOR_KG_PER_KWH = new BigDecimal("0.045");
    private static final BigDecimal KG_PER_CREDIT = BigDecimal.ONE; // 1 credit per kg CO2e avoided
    private static final int RAW_SCALE = 6;
    private static final int ROUNDED_SCALE = 2;

    public DefaultIssuanceService(
            CreditIssuanceRepository repository,
            WalletClient walletClient,
            RetryTemplate externalRetryTemplate,
            OutboxService outboxService
    ) {
        this.repository = repository;
        this.walletClient = walletClient;
        this.externalRetryTemplate = externalRetryTemplate;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public CreditIssuance issueCredits(VerificationRequest request, String idempotencyKey, String correlationId) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Verification request must be persisted before issuance");
        }
        String sanitizedIdempotencyKey = idempotencyKey != null ? idempotencyKey.trim() : null;
        if (!StringUtils.hasText(sanitizedIdempotencyKey)) {
            throw new DomainValidationException(
                "Missing idempotency key",
                java.util.List.of("X-Idempotency-Key header or payload field is required")
            );
        }

        String sanitizedCorrelationId = correlationId != null ? correlationId.trim() : null;
        if (!StringUtils.hasText(sanitizedCorrelationId)) {
            sanitizedCorrelationId = null;
        }

        Optional<CreditIssuance> existing = repository.findByIdempotencyKey(sanitizedIdempotencyKey);
        if (existing.isPresent()) {
            CreditIssuance issuance = existing.get();
            if (!issuance.getVerificationRequest().getId().equals(request.getId())) {
                throw new DomainValidationException(
                    "Idempotency key already used",
                    java.util.List.of("Provided idempotency key belongs to another verification request")
                );
            }
            log.info("Returning existing issuance for request {} via idempotency key {}", request.getId(), idempotencyKey);
            return issuance;
        }

        BigDecimal baselineEmission = request.getDistanceKm()
            .multiply(ICE_EMISSION_FACTOR_KG_PER_KM)
            .setScale(RAW_SCALE, RoundingMode.HALF_UP);
        BigDecimal evEmission = request.getEnergyKwh()
            .multiply(GRID_EMISSION_FACTOR_KG_PER_KWH)
            .setScale(RAW_SCALE, RoundingMode.HALF_UP);
        BigDecimal co2Reduced = baselineEmission.subtract(evEmission);
        if (co2Reduced.signum() < 0) {
            co2Reduced = BigDecimal.ZERO.setScale(RAW_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal creditsRaw = co2Reduced.divide(KG_PER_CREDIT, RAW_SCALE, RoundingMode.HALF_UP);
        BigDecimal creditsRounded = creditsRaw.setScale(ROUNDED_SCALE, RoundingMode.HALF_EVEN);

        CreditIssuance issuance = new CreditIssuance();
        issuance.setVerificationRequest(request);
        issuance.setOwnerId(request.getOwnerId());
        issuance.setCo2ReducedKg(co2Reduced);
        issuance.setCreditsRaw(creditsRaw);
        issuance.setCreditsRounded(creditsRounded);
        issuance.setIdempotencyKey(sanitizedIdempotencyKey);
        issuance.setCorrelationId(sanitizedCorrelationId);

        CreditIssuance saved = repository.save(issuance);

        MDC.put("vrId", request.getId().toString());
        final String resolvedCorrelationId = sanitizedCorrelationId;
        try {
            externalRetryTemplate.execute(context -> {
                walletClient.credit(request.getOwnerId(), creditsRounded, resolvedCorrelationId, sanitizedIdempotencyKey);
                return null;
            });
            log.info("Issued {} credits (rounded) for request {}", creditsRounded, request.getId());
        } catch (Exception ex) {
            String correlationValue = resolvedCorrelationId != null
                ? resolvedCorrelationId
                : CorrelationIdHolder.get().orElse(null);
            outboxService.enqueueWalletCredit(
                new WalletCreditOutboxPayload(request.getOwnerId(), creditsRounded, correlationValue, sanitizedIdempotencyKey),
                correlationValue,
                sanitizedIdempotencyKey
            );
            log.warn("Wallet credit deferred to outbox for request {}: {}", request.getId(), ex.getMessage());
        } finally {
            MDC.remove("vrId");
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditIssuance> getByRequestId(UUID requestId) {
        return repository.findByVerificationRequest_Id(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditIssuance> getByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CreditIssuance> search(CreditIssuanceQuery query, Pageable pageable) {
        return repository.findAll(CreditIssuanceSpecifications.fromQuery(query), pageable);
    }
}
