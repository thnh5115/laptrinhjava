package ccm.cva.issuance.application.service;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.wallet.client.WalletClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultIssuanceService implements IssuanceService {

    private final CreditIssuanceRepository repository;
    private final WalletClient walletClient;

    private static final Logger log = LoggerFactory.getLogger(DefaultIssuanceService.class);

    private static final BigDecimal ICE_EMISSION_FACTOR_KG_PER_KM = new BigDecimal("0.120");
    private static final BigDecimal GRID_EMISSION_FACTOR_KG_PER_KWH = new BigDecimal("0.045");
    private static final BigDecimal KG_PER_CREDIT = BigDecimal.ONE; // 1 credit per kg CO2e avoided
    private static final int RAW_SCALE = 6;
    private static final int ROUNDED_SCALE = 2;

    public DefaultIssuanceService(CreditIssuanceRepository repository, WalletClient walletClient) {
        this.repository = repository;
        this.walletClient = walletClient;
    }

    @Override
    @Transactional
    public CreditIssuance issueCredits(VerificationRequest request, String idempotencyKey, String correlationId) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Verification request must be persisted before issuance");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new DomainValidationException(
                "Missing idempotency key",
                java.util.List.of("X-Idempotency-Key header or payload field is required")
            );
        }

        Optional<CreditIssuance> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            CreditIssuance issuance = existing.get();
            if (!issuance.getVerificationRequest().getId().equals(request.getId())) {
                throw new DomainValidationException(
                    "Idempotency key already used",
                    java.util.List.of("Provided idempotency key belongs to another verification request")
                );
            }
            log.debug("Replaying issuance for request {} via idempotency key {}", request.getId(), idempotencyKey);
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
        issuance.setIdempotencyKey(idempotencyKey);
        issuance.setCorrelationId(correlationId);

        CreditIssuance saved = repository.save(issuance);
        // Credit wallet; any failure should bubble up to roll back the transaction.
        walletClient.credit(request.getOwnerId(), creditsRounded, correlationId, idempotencyKey);
        log.info("Issued {} credits (rounded) for request {}", creditsRounded, request.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditIssuance> getByRequestId(Long requestId) {
        return repository.findByVerificationRequest_Id(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditIssuance> getByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey);
    }
}
