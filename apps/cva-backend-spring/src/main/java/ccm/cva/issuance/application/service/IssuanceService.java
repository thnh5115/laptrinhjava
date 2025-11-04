package ccm.cva.issuance.application.service;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.verification.domain.VerificationRequest;
import java.math.BigDecimal;
import java.util.UUID;

public interface IssuanceService {

    CreditIssuance draftIssuance(VerificationRequest request, BigDecimal co2ReducedKg, BigDecimal creditsRaw,
                                 BigDecimal creditsRounded, String idempotencyKey, String correlationId);

    CreditIssuance getByRequestId(UUID requestId);
}
