package ccm.cva.issuance.application.service;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.verification.domain.VerificationRequest;

import java.util.Optional;

public interface IssuanceService {

    CreditIssuance issueCredits(VerificationRequest request, String idempotencyKey, String correlationId);

    Optional<CreditIssuance> getByRequestId(Long requestId);

    Optional<CreditIssuance> getByIdempotencyKey(String idempotencyKey);
}
