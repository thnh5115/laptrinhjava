package ccm.cva.issuance.application.service;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.verification.domain.VerificationRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultIssuanceService implements IssuanceService {

    private final CreditIssuanceRepository repository;

    public DefaultIssuanceService(CreditIssuanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreditIssuance draftIssuance(VerificationRequest request, BigDecimal co2ReducedKg, BigDecimal creditsRaw,
                                        BigDecimal creditsRounded, String idempotencyKey, String correlationId) {
        throw new UnsupportedOperationException("Issuance workflow will be provided in week 2 implementation");
    }

    @Override
    public CreditIssuance getByRequestId(UUID requestId) {
        return repository.findByVerificationRequest_Id(requestId)
            .orElseThrow(() -> new UnsupportedOperationException("Week 2: implement retrieval by request"));
    }
}
