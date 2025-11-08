package ccm.cva.issuance.application.service;

import ccm.cva.issuance.application.query.CreditIssuanceQuery;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.verification.domain.VerificationRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IssuanceService {

    CreditIssuance issueCredits(VerificationRequest request, String idempotencyKey, String correlationId);

    Optional<CreditIssuance> getByRequestId(UUID requestId);

    Optional<CreditIssuance> getByIdempotencyKey(String idempotencyKey);

    Page<CreditIssuance> search(CreditIssuanceQuery query, Pageable pageable);
}
